package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.*;
import com.haegreen.fishing.entitiy.Reservation;
import com.haegreen.fishing.entitiy.ReservationDate;
import com.haegreen.fishing.repository.ReservationDateRepository;
import com.haegreen.fishing.repository.ReservationRepository;
import com.haegreen.fishing.service.ReservationDateService;
import com.haegreen.fishing.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("admin")
@Log4j2
@RequiredArgsConstructor
public class AdminController {
    private final ReservationService reservationService;
    private final ReservationDateService reservationDateService;
    private final ReservationRepository reservationRepository;
    private final ReservationDateRepository reservationDateRepository;

    public String numberFormat(int money) {
        NumberFormat formatter = NumberFormat.getInstance();
        return formatter.format(money);
    }

    private Boolean sendSms(String regName, String to, LocalDate regDate, int mebmber) {
        String api_key = "NCS04BSHXUV9PONO";
        String api_secret = "I60QMWVQ0AZLLANTGFNHSQ64RI9EJERL";
        Message coolsms = new Message(api_key, api_secret);
        HashMap<String, String> params = new HashMap<>();
        String from = "010-4421-2628";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 ");
        String formattedDate = regDate.format(formatter);
        String text = regName + "님 " + formattedDate + mebmber + "명 예약확인이 완료되었습니다.(해그린피싱 거북선호)";
        params.put("to", to);
        params.put("from", from);
        params.put("type", "SMS");
        params.put("text", text);
        params.put("app_version", "test app 1.2");
        try {
            JSONObject obj = coolsms.send(params);
            System.out.println(obj.toString());
            return Boolean.TRUE;
        } catch (CoolsmsException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
            return Boolean.FALSE;
        }
    }

    @GetMapping("reservationList")
    public ResponseEntity<?> list(@RequestParam(value = "regDate") LocalDate regDate, Map<String, Object> dataMap) {
        List<ReservationDTO> reservationDTOS = reservationService.getDateUserReservation(regDate);
        int confirmMember = 0;
        for (ReservationDTO r : reservationDTOS) {
            r.setFormatMoney(numberFormat(r.getMoney()));
            if (r.isState())
                confirmMember += r.getMember();
        }
        dataMap.put("confirmMember", Integer.valueOf(confirmMember));
        dataMap.put("reservationDTOS", reservationDTOS);

        ReservationDate reservationDate = reservationDateRepository.findReservationDateByRegDate(regDate);
        boolean available = reservationDateService.isReservable(reservationDate);
        int extras = reservationDate.getExtrasMembers() - confirmMember;

        dataMap.put("fishingMoney", reservationDate.getFishingMoney());
        dataMap.put("available", available);
        dataMap.put("extras", Integer.valueOf(extras));
        return ResponseEntity.ok(dataMap);
    }

    @GetMapping("searchList")
    public ResponseEntity<?> getSearchList(PageRequestDTO pageRequestDTO, ReservationDTO reservationDTO) {
        pageRequestDTO.setSize(15);
        PageResultDTO<ReservationDTO, Object[]> reservation = reservationService.getSearchList(pageRequestDTO, reservationDTO.getRvno(), reservationDTO.getRegName(), reservationDTO
                .getDepositName(), reservationDTO.getTel(), reservationDTO.getRegDate());

        int confirmMember = 0;
        int extras = 0;
        for (ReservationDTO r : reservation.getDtoList()) {
            r.setFormatMoney(numberFormat(r.getMoney()));
            if (r.isState())
                confirmMember += r.getMember();
            extras = r.getExtraMembers() - confirmMember;
        }

        if (reservation.getTotalPage() == 0)
            reservation.setTotalPage(1);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("extras", extras);
        dataMap.put("confirmMember", confirmMember);
        dataMap.put("result", reservation);
        return ResponseEntity.ok(dataMap);
    }

    @PostMapping("register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody ReservationDTO dto) {
        String tel = dto.getTel1() + "-" + dto.getTel2() + "-" + dto.getTel3();
        dto.setTel(tel);
        Long rvno = reservationService.register(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", rvno + "번 예약 등록이 성공적으로 완료되었습니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("reservationState")
    public ResponseEntity<?> modifyState(@RequestBody ReservationDTO reservationDTO) {
        ResponseDTO responseDTO = new ResponseDTO();
        Optional<Reservation> reservation = reservationService.modifyState(reservationDTO.getRvno(), reservationDTO);
        if (reservation.isEmpty()) {
            responseDTO.setError("조회에 실패하였습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
        }
        if (sendSms(reservation.get().getRegName(), reservation.get().getTel(), reservation.get().getRegDate(), reservation.get().getMember())) {
            responseDTO.setSuccess("예약 확정 및 문자가 발송되었습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
        responseDTO.setError("발송에 실패하였습니다.");
        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("reservationInfo")
    public ResponseEntity<?> reservationInfo(@RequestParam("rvno") Long rvno) {
        ReservationDTO reservationDTO = reservationService.get(rvno);
        reservationDTO.setFormatMoney(numberFormat(reservationDTO.getMoney()));

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("reservationDTO", reservationDTO);
        return ResponseEntity.ok(dataMap);
    }

    @GetMapping("changeDate")
    public ResponseEntity<?> changeDate(@RequestParam("regDate") LocalDate regDate){

        Integer confirmedMembers = reservationRepository.findConfirmedReservationsOnDate(regDate);
        int extras = 16 - (confirmedMembers != null ? confirmedMembers : 0);
        Map<String, Integer> responseMap = new HashMap<>();
        responseMap.put("extras", extras);
        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }

    @GetMapping("reservationModify")
    public ResponseEntity<?> showReservationModifyForm(@RequestParam("rvno") Long rvno) {
        ReservationDTO reservationDTO = reservationService.get(rvno);
        ReservationDate reservationDate = reservationDateRepository.findReservationDateByRegDate(reservationDTO.getRegDate());
        Integer confirmedMembers = reservationRepository.findConfirmedReservationsOnDate(reservationDTO.getRegDate());
        int extras = reservationDate.getExtrasMembers() - (confirmedMembers != null ? confirmedMembers : 0);
        extras += reservationDTO.getMember();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("extras", extras);
        String[] parts = reservationDTO.getTel().split("-");
        reservationDTO.setTel1(parts[0]);
        reservationDTO.setTel2(parts[1]);
        reservationDTO.setTel3(parts[2]);
        dataMap.put("reservationDTO", reservationDTO);
        return ResponseEntity.ok(dataMap);
    }

    @GetMapping("getDateState")
    public ResponseEntity<?> List(@RequestParam("regDate") LocalDate regDate) {
        ReservationDateDTO reservationDateDTO = reservationDateService.getReservationDate(regDate);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("reservationDateDTO", reservationDateDTO);
        return ResponseEntity.ok(dataMap);
    }

    @PostMapping("modifyDateState")
    public ResponseEntity<?> modifyAvailable(ReservationDateDTO reservationDateDTO) {
        ResponseDTO responseDTO = new ResponseDTO();
        boolean isModified = reservationDateService.modifyDateAvailable(reservationDateDTO.getRdate(), reservationDateDTO);
        if (isModified) {
            responseDTO.setSuccess("변경 되었습니다.");
            return ResponseEntity.ok(responseDTO);
        }
        responseDTO.setError("변경에 실패하였습니다.");
        return ResponseEntity.badRequest().body(responseDTO);
    }

    @PostMapping("moneyChange")
    public ResponseEntity<?> moneyChange(ReservationDTO reservationDTO) {
        ResponseDTO responseDTO = new ResponseDTO();
        if (reservationService.modifyMoney(reservationDTO)) {
            responseDTO.setSuccess("입금금액이 수정되었습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
        responseDTO.setError("수정 실패");
        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("reservationModify")
    public ResponseEntity<?> reservationModify(@RequestBody  ReservationDTO reservationDTO) {
        String tel = reservationDTO.getTel1() + "-" + reservationDTO.getTel2() + "-" + reservationDTO.getTel3();
        reservationDTO.setTel(tel);
        reservationService.modify(reservationDTO);
        return ResponseEntity.ok("수정이 되었습니다.");
    }

    @PostMapping("reservationDelete")
    public ResponseEntity<?> reservationDelete(@RequestBody ReservationDTO reservationDTO) {
        ResponseDTO responseDTO = new ResponseDTO();
        if (reservationService.remove(reservationDTO.getRvno())) {
            responseDTO.setSuccess("삭제 성공");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
        responseDTO.setError("삭제 실패");
        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("modifySort")
    public ResponseEntity<?> modifySortsGet(Model model, ReservationDateDTO reservationDateDTO){
        reservationDateDTO = reservationDateService.getReservationDate(reservationDateDTO.getRegDate());

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("reservationDateDTO", reservationDateDTO);
        return ResponseEntity.ok(dataMap);
    }

    @PostMapping("modifySort")
    public ResponseEntity<ResponseDTO> modifySortsPost(@RequestBody ReservationDateDTO reservationDateDTO) {

        ResponseDTO responseDTO = new ResponseDTO();
        LocalDate startDate = reservationDateDTO.getStartDate();
        LocalDate endDate = reservationDateDTO.getEndDate();

        if (startDate == null || endDate == null) {
            responseDTO.setError("시작날짜와 종료날짜를 모두 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateDTO.getFishingSort() == null || reservationDateDTO.getFishingSort().trim().isEmpty()) {
            responseDTO.setError("낚시 종류를 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateDTO.getExtrasMembers() == null) {
            responseDTO.setError("낚시 인원을 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateService.modifySorts(startDate, endDate, reservationDateDTO.getFishingSort(),
                reservationDateDTO.getExtrasMembers(), reservationDateDTO.getFishingMoney())) {
            responseDTO.setSuccess("적용되었습니다. 예약현황보기로 이동 하시겠습니까?");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            responseDTO.setError("수정 중 문제가 발생했습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("modifySort2")
    public ResponseEntity<ResponseDTO> modifySortPost(@RequestBody ReservationDateDTO reservationDateDTO) {

        ResponseDTO responseDTO = new ResponseDTO();

        if (reservationDateDTO.getFishingSort() == null || reservationDateDTO.getFishingSort().trim().isEmpty()) {
            responseDTO.setError("낚시 종류를 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        if (reservationDateDTO.getExtrasMembers() == null) {
            responseDTO.setError("낚시 인원을 입력해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }


        if (reservationDateService.modifySort(reservationDateDTO.getRdate(), reservationDateDTO)) {
            responseDTO.setSuccess("적용되었습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            responseDTO.setError("수정 중 문제가 발생했습니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package com.haegreen.fishing.controller;

import com.haegreen.fishing.constant.Role;
import com.haegreen.fishing.dto.MemberFormDto;
import com.haegreen.fishing.dto.ReservationDTO;
import com.haegreen.fishing.dto.ReservationDateDTO;
import com.haegreen.fishing.dto.ResponseDTO;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.entitiy.Reservation;
import com.haegreen.fishing.entitiy.ReservationDate;
import com.haegreen.fishing.repository.ReservationDateRepository;
import com.haegreen.fishing.repository.ReservationRepository;
import com.haegreen.fishing.security.CustomUserDetails;
import com.haegreen.fishing.service.ReservationDateService;
import com.haegreen.fishing.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("reservation")
@Log4j2
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationDateService reservationDateService;
    private final ReservationRepository reservationRepository;
    private final ReservationDateRepository reservationDateRepository;

    private Boolean sendSms(String regName, LocalDate regDate, int mebmber) {
        String api_key = "NCS04BSHXUV9PONO";
        String api_secret = "I60QMWVQ0AZLLANTGFNHSQ64RI9EJERL";
        Message coolsms = new Message(api_key, api_secret);
        HashMap<String, String> params = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 ");
        String formattedDate = regDate.format(formatter);
        String text = regName + "님 " + formattedDate + mebmber + "명 예약이 등록되었습니다.";
        params.put("to", "010-4421-2628");
        params.put("from", "010-4421-2628");
        params.put("type", "SMS");
        params.put("text", text);
        params.put("app_version", "test app 1.2");
        try {
            JSONObject obj = coolsms.send(params);
            log.info(obj.toString());
            return Boolean.TRUE;
        } catch (CoolsmsException e) {
            log.info(e.getMessage());
            log.info(e.getCode());
            return Boolean.FALSE;
        }
    }

    @GetMapping("list")
    public ResponseEntity<?> list(Map<String, Object> dataMap) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(1);
        LocalDate endDate = today.plusMonths(3);
        List<ReservationDTO> reservationDTOS = reservationService.getAllReservations(startDate, endDate);
        List<ReservationDateDTO> reservationDateDTOS = reservationDateService.getAllReservationDates(startDate, endDate);

        LocalTime currentTime = LocalTime.now();
        for(ReservationDateDTO reservationDateDTO : reservationDateDTOS){
            String fishingSort = reservationDateDTO.getFishingSort();
            LocalDate regDate = reservationDateDTO.getRegDate();

            // 조건에 따라 available 필드 업데이트
            if (!reservationDateDTO.isAvailable() && //false 조건중
                    regDate.equals(today) && // 오늘날짜면
                    !reservationDateDTO.isDateModify() && // 수정이 없으면
                    fishingSort != null && // 어업종류가 비지 있지 않으면
                    "갈치".contains(fishingSort) && //갈치를
                    currentTime.isBefore(LocalTime.of(14, 0))) { //2시이전에
                reservationDateDTO.setAvailable(true); // true로
            }
        }

        dataMap.put("reservationDTOS", reservationDTOS);
        dataMap.put("reservationDateDTOS", reservationDateDTOS);
        return ResponseEntity.ok(dataMap);
    }

    @GetMapping("register")
    public ResponseEntity<?> register(
            Authentication authentication,
            Map<String, Object> dataMap,
            @RequestParam(value = "regDate") LocalDate regDate) {

        ReservationDate reservationDate = reservationDateRepository.findReservationDateByRegDate(regDate);
        Integer confirmedMembers = reservationRepository.findConfirmedReservationsOnDate(regDate);

        int extras = reservationDate.getExtrasMembers() - (confirmedMembers != null ? confirmedMembers : 0);
        if (extras <= 0 || !reservationDateService.isReservable(reservationDate)) {
            return ResponseEntity.badRequest().body("잘못된 요청");
        }

        dataMap.put("fishingMoney", reservationDate.getFishingMoney());
        dataMap.put("extras", extras);

        // 인증이 확인되면 로그인한 사용자의 정보를 가져온다.
        MemberFormDto memberFormDto = new MemberFormDto();
        if (authentication != null && authentication.isAuthenticated()) {
            Member member = ((CustomUserDetails) authentication.getPrincipal()).getMember();

            if (member.getRole() != Role.ADMIN) {
                // 관리자가 아닌 경우, 사용자 정보를 memberFormDto에 채운다.
                populateMemberFormDtoFromMember(memberFormDto, member);
            }
            dataMap.put("memberFormDto", memberFormDto);

        } else {
            // 로그인하지 않은 경우, 기본 memberFormDto를 모델에 추가한다.
            dataMap.put("memberFormDto", memberFormDto);
        }

        return ResponseEntity.ok(dataMap);
    }

    // 메서드를 분리이용하여 재사용성을 높임
    private void populateMemberFormDtoFromMember(MemberFormDto memberFormDto, Member member) {
        memberFormDto.setName(member.getName());
        memberFormDto.setEmail(member.getEmail());

        // 전화번호를 분리한다.
        String[] parts = member.getTel().split("-");
        memberFormDto.setTel1(parts[0]);
        memberFormDto.setTel2(parts[1]);
        memberFormDto.setTel3(parts[2]);
    }


    @PostMapping("register")
    public ResponseEntity<?> register(@RequestBody ReservationDTO dto, Authentication authentication) {
        ResponseDTO responseDTO = new ResponseDTO();

        String tel = dto.getTel1() + "-" + dto.getTel2() + "-" + dto.getTel3();
        dto.setTel(tel);

        if (dto.getMember() == 0) {
            responseDTO.setError("0명 예약은 불가능합니다.");
            return ResponseEntity.badRequest().body(responseDTO);
        }

        Long rvno = reservationService.register(dto);

        if (authentication != null && authentication.isAuthenticated()) {
            Member member = ((CustomUserDetails) authentication.getPrincipal()).getMember();
            if (member.getRole() != Role.ADMIN) {
                sendSms(dto.getRegName(), dto.getRegDate(), dto.getMember()); // 관리자가 아닌 사람이 등록시, 관리자에게 알림 문자 발송
            }
        } else {
            sendSms(dto.getRegName(), dto.getRegDate(), dto.getMember());
        }
        responseDTO.setSuccess(String.valueOf(rvno));

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("success")
    public void success(Model model) {

    }

    @PostMapping("check")
    public ResponseEntity<?> checkReservation(@RequestBody ReservationDTO reservationDTO, ResponseDTO responseDTO) { // 로그인 하지 않은 사람의 예약내역 조회

        String tel = reservationDTO.getTel1() + "-" + reservationDTO.getTel2() + "-" + reservationDTO.getTel3();
        reservationDTO.setTel(tel);

        if (reservationService.check(reservationDTO.getEmail(), reservationDTO.getRegName(), reservationDTO.getTel())) {
            responseDTO.setDataObject(reservationDTO);
            return ResponseEntity.ok(responseDTO);
        } else {
            responseDTO.setError("입력하신 정보로 조회된 예약 내역이 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDTO);
        }
    }

    @PostMapping("details")
    public ResponseEntity<?> reservationlist(@RequestBody ReservationDTO reservationDTO, Authentication authentication) {

        String tel = reservationDTO.getTel1() + "-" + reservationDTO.getTel2() + "-" + reservationDTO.getTel3();
        reservationDTO.setTel(tel);

        List<ReservationDTO> reservationDTOS;
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("today", LocalDate.now());

        if (authentication != null && authentication.isAuthenticated()) {
            reservationDTOS = reservationService.getUserReservation(authentication, null);
        } else {
            reservationDTOS = reservationService.getUserReservation(null, reservationDTO);
        }

        if (reservationDTOS.isEmpty()) {
            dataMap.put("message", "예약된 내역이 없습니다.");
        } else {
            dataMap.put("reservationlist", reservationDTOS);
        }

        return ResponseEntity.ok(dataMap);
    }


}

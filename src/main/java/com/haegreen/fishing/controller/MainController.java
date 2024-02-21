package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.*;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.security.CustomUserDetails;
import com.haegreen.fishing.security.TokenProvider;
import com.haegreen.fishing.service.JowhangBoardService;
import com.haegreen.fishing.service.ReservationDateService;
import com.haegreen.fishing.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("")
@Log4j2
@RequiredArgsConstructor
public class MainController {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private final ReservationService reservationService;
    private final ReservationDateService reservationDateService;
    private final JowhangBoardService jowhangBoardService;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    @GetMapping("")
    public ResponseEntity<?> oauthSuccess(@RequestParam(value = "oauth", required = false) String oauth){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MemberFormDto memberFormDto = new MemberFormDto();
        if (principal instanceof CustomUserDetails && Objects.equals(oauth, "true")) {
            Member member = ((CustomUserDetails) principal).getMember();
            String token = tokenProvider.create((CustomUserDetails) principal, secretKey);
            memberFormDto.setRole(member.getRole());
            memberFormDto.setToken(token);
            String refreshToken = tokenProvider.createRefreshToken((CustomUserDetails) principal, secretKey);
            member.setRefreshToken(refreshToken);
            memberRepository.save(member);
        }
        return ResponseEntity.ok(memberFormDto);
    }

    @GetMapping(value = "main")
    public ResponseEntity<?> mainPage(Map<String, Object> dataMap) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(9);
        List<ReservationDTO> reservationDTOS = reservationService.getAllReservations(today, endDate);
        List<ReservationDateDTO> reservationDateDTOS = reservationDateService.getAllReservationDates(today, endDate);

        // 갈치 시간 바꿈
        LocalTime currentTime = LocalTime.now();
        for(ReservationDateDTO reservationDateDTO : reservationDateDTOS){
            String fishingSort = reservationDateDTO.getFishingSort();
            LocalDate regDate = reservationDateDTO.getRegDate();

            // 조건에 따라 available 필드 업데이트
            if (!reservationDateDTO.isAvailable() && //false 조건중
                    regDate.equals(today) && // 오늘 날짜면
                    !reservationDateDTO.isDateModify() && // 날짜 수정이 없으면
                    fishingSort != null && // 어업종류가 비지 있지 않으면
                    "갈치".contains(fishingSort) && //갈치를
                    currentTime.isBefore(LocalTime.of(14, 0))) { //2시이전에
                reservationDateDTO.setAvailable(true); // true로
            }
        }

        dataMap.put("reservationDTOS", reservationDTOS);
        dataMap.put("reservationDateDTOS", reservationDateDTOS);

        int page = 1;
        int pageSize = 10;

        PageRequestDTO pageRequestDTO = new PageRequestDTO(page, pageSize);
        PageResultDTO<JowhangBoardDTO, Object[]> jowhangBoard = jowhangBoardService.getList(pageRequestDTO);
        dataMap.put("jowhangBoard", jowhangBoard);

        return ResponseEntity.ok(dataMap);
    }

    @PostMapping(value = "authlocal")
    private ResponseEntity<?> authLocal(MemberFormDto memberFormDto, Authentication authentication,
                                   HttpServletRequest request, HttpServletResponse response) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            Member member = ((CustomUserDetails) principal).getMember();
            log.info("test : " + member.getEmail() );

            if (tokenProvider.isTokenExpired(member.getRefreshToken(), secretKey)) {
                ResponseDTO responseDTO = new ResponseDTO();
                responseDTO.setError("재로그인이 필요합니다.");
                SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
                securityContextLogoutHandler.setInvalidateHttpSession(true); // 세션 무효화 처리
                securityContextLogoutHandler.logout(request, response, authentication); // 로그아웃 처리
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
            }

            String token = tokenProvider.create((CustomUserDetails) principal, secretKey);
            memberFormDto.setRole(member.getRole());
            memberFormDto.setToken(token);
        } else {
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setError("인증되지 않은 사용자 또는 세션이 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
        }

        return ResponseEntity.ok(memberFormDto);
    }

    @PostMapping(value = "authsession")
    private ResponseEntity<?> authSession(MemberFormDto memberFormDto) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            Member member = ((CustomUserDetails) principal).getMember();
            String token = tokenProvider.create((CustomUserDetails) principal, secretKey);
            memberFormDto.setRole(member.getRole());
            memberFormDto.setToken(token);
        } else {
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setError("인증되지 않은 사용자 또는 세션이 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
        }

        return ResponseEntity.ok(memberFormDto);
    }
}

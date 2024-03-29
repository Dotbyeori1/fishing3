package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.MemberFormDto;
import com.haegreen.fishing.dto.ResponseDTO;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.security.CustomUserDetails;
import com.haegreen.fishing.security.TokenProvider;
import com.haegreen.fishing.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.util.Optional;

@RequestMapping("member")
@Controller
@Log4j2
@RequiredArgsConstructor
public class MemberController {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping(value = "join")
    public ResponseEntity<?> memberForm(@Valid @RequestBody MemberFormDto memberFormDto){

        String tel = memberFormDto.getTel1() + "-" + memberFormDto.getTel2() + "-" + memberFormDto.getTel3();
        memberFormDto.setTel(tel);

        if (memberFormDto.getPassword().length() <= 5) {
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setError("비밀번호는 6자리 이상으로 해주세요.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setError("이미 회원가입 된 계정입니다.");
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setSuccess("회원가입이 완료되었습니다.");
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody MemberFormDto memberFormDto, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Authentication authentication; // 스프링 시큐리티 로그인 객체 불러오기

        try { // 스프링 시큐리티를 이용한 로그인 확인.
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            memberFormDto.getEmail(),
                            memberFormDto.getPassword()
                    )
            );
        } catch (BadCredentialsException e) { // 틀렸을때 예외 처리
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setError("아이디나 패스워드가 틀립니다.");
            return ResponseEntity.badRequest().body(responseDTO);
        } // 틀리면 걍 객체를 반환 시키는게 더 빠름 - ajax 처리.

        // 맞으면 인증, 권한 부여하기
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        securityContextRepository.saveContext(securityContext, request, response); // 인증 저장하기
        Member member = ((CustomUserDetails) authentication.getPrincipal()).getMember();
        // token을 만들어서 발송하는 부분
        String token = tokenProvider.create(customUserDetails, secretKey);
        MemberFormDto responseMemberFormDto = new MemberFormDto();
        responseMemberFormDto.setToken(token);
        responseMemberFormDto.setRole(member.getRole());

        if (memberFormDto.isCheck15()) { // 자동로그인 체크
            String refreshToken = tokenProvider.createRefreshToken(customUserDetails, secretKey);
            member.setRefreshToken(refreshToken);
            memberRepository.save(member);
        }

        return ResponseEntity.ok(responseMemberFormDto);
    }

    @GetMapping("memberinfo")
    public ResponseEntity<?> memberInfo() {
        ResponseDTO responseDTO = new ResponseDTO();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            responseDTO.setError("로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(responseDTO);
        }

        Member member = ((CustomUserDetails) principal).getMember();

        responseDTO.setDataObject(member);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping(value = "update")
    public ResponseEntity<?> memberInfoEdit() {
        ResponseDTO responseDTO = new ResponseDTO();
        MemberFormDto memberFormDto = new MemberFormDto();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            responseDTO.setError("로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(responseDTO);
        }

        Member member = ((CustomUserDetails) principal).getMember();

        memberFormDto.setEmail(member.getEmail());
        memberFormDto.setName(member.getName());
        memberFormDto.setNickName(member.getNickName());

        // 전화번호 split
        String tel = member.getTel();
        String[] parts = tel.split("-");
        String tel1 = parts[0];
        String tel2 = parts[1];
        String tel3 = parts[2];
        memberFormDto.setTel1(tel1);
        memberFormDto.setTel2(tel2);
        memberFormDto.setTel3(tel3);

       responseDTO.setDataObject(memberFormDto);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping(value = "update")
    public ResponseEntity<?> editProfile(@RequestBody MemberFormDto memberFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        String tel = memberFormDto.getTel1() + "-" + memberFormDto.getTel2() + "-" + memberFormDto.getTel3();
        memberFormDto.setTel(tel);

        // 세이브와 동시에 스프링 시큐리티 반영
        CustomUserDetails customUserDetails = new CustomUserDetails(memberService.editMember(memberFormDto));
        SecurityContextHolder.getContext().setAuthentication
                (new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities()));

        return ResponseEntity.ok("변경되었습니다.");
    }

    @PostMapping(value = "changepw")
    public ResponseEntity<?> changePassword(@RequestBody MemberFormDto memberFormDto) {
        ResponseDTO responseDTO = new ResponseDTO();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            responseDTO.setError("로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(responseDTO);
        }

        Member member = ((CustomUserDetails) principal).getMember();

        if (!passwordEncoder.matches(memberFormDto.getPassword(), member.getPassword())) {
            responseDTO.setError("현재 비밀번호가 틀립니다.");
            return ResponseEntity.badRequest().body(responseDTO);
        }

        memberFormDto.setEmail(member.getEmail());
        CustomUserDetails customUserDetails = new CustomUserDetails(memberService.changePassword(memberFormDto));
        SecurityContextHolder.getContext().setAuthentication
                (new UsernamePasswordAuthenticationToken(customUserDetails, customUserDetails.getPassword(), customUserDetails.getAuthorities()));
        responseDTO.setSuccess("비밀번호 변경 성공");

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping(value = "point")
    public ResponseEntity<?> point() {
        ResponseDTO responseDTO = new ResponseDTO();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            responseDTO.setError("로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(responseDTO);
        }

        Member member = ((CustomUserDetails) principal).getMember();
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setPoint(member.getPoint());
        responseDTO.setDataObject(memberFormDto);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping(value = "forgotpw")
    public String forgotpw(Model model) {
        return "member/forgotpw";
    }

    @PostMapping(value = "forgotpw")
    public String sendNewpw(Model model, @RequestParam String email) {

        Optional<Member> memberOptional = Optional.ofNullable(memberRepository.findByEmail(email));

        //매개변수로 받은 이메일을 findByEmail메서드로 멤버객체를 찾음
        if (memberOptional.isPresent()) { //해당 이메일로 가입한 멤버객체가 있으면
            Member member = memberOptional.get();
            String newPassword = generateRandomPassword(); //비밀번호 생성 메서드 필요
            member.setPassword(passwordEncoder.encode(newPassword)); //새 비밀번호를 암호화해 멤버객체를 변경
            memberRepository.save(member); //변경된 멤버정보 저장

            memberService.sendEmail(
                    email,
                    "해그린피싱 비밀번호 안내",
                    "초기화된 비밀번호 : " + newPassword + "\n" + "※ 비밀번호 초기화 후에는 비밀번호 재설정이 필요합니다 ※"
            );
            model.addAttribute("message", "변경된 비밀번호가 이메일로 전송되었습니다. 변경된 비밀번호를 입력 후 비밀번호 변경에서 비밀번호를 변경해주세요.");
            //정상전송되면 성공 메시지 띄우기
        } else {
            model.addAttribute("error", "이메일 전송 실패");
        } //실패하면 에러메시지 띄우기

        return "member/forgotpw";
    }

    private String generateRandomPassword() {
        int length = 10; // 비밀번호 길이
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        //비밀번호에 들어갈 수 있는 알파벳,숫자 설정
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) { //10자리까지 랜덤하게 요소를 추출해 새로운 비밀번호 설정
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }
} //class
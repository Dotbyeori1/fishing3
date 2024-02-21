package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.*;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.service.JowhangBoardImgService;
import com.haegreen.fishing.service.JowhangBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("jowhangboard")
@Log4j2
@RequiredArgsConstructor
public class JowhangBoardController {

    private final JowhangBoardService jowhangBoardService;
    private final JowhangBoardImgService jowhangBoardImgService;
    private final MemberRepository memberRepository;

    @GetMapping("list")
    public ResponseEntity<?> list(PageRequestDTO pageRequestDTO) {

        PageResultDTO<JowhangBoardDTO, Object[]> pageResultDTO = jowhangBoardService.getList(pageRequestDTO);
        if (pageResultDTO.getTotalPage() == 0) {
            pageResultDTO.setTotalPage(1);
        } // 글이 하나도 없을 땐 0으로 인식하므로

        pageRequestDTO.setType(pageRequestDTO.getTypeAsString());

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("pageRequestDTO", pageRequestDTO);
        dataMap.put("result", pageResultDTO);

        return ResponseEntity.ok(dataMap);
    }

    @PostMapping("register")
    public ResponseEntity<?> registerPost(JowhangBoardDTO dto, Authentication authentication, ResponseDTO responseDTO,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        Member member = memberRepository.findByEmail(authentication.getName());

        dto.setWriterEmail(member.getEmail());

        Long jbno = jowhangBoardService.register(dto); //새로 추가된 엔티티의 번호(dto)

        if (images != null) {
            images.forEach(i -> {
                jowhangBoardImgService.jowhangBoardRegister(i, jbno);
            });
        }

        responseDTO.setSuccess(jbno+"번이 등록되었습니다.");

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping({"read", "modify"})
    public ResponseEntity<?> read(@RequestParam("jbno") Long jbno) {

        JowhangBoardDTO jowhangBoardDTO = jowhangBoardService.get(jbno);

        List<ImgDTO> jowhangBoardImgDTOs = jowhangBoardService.getImgList(jbno);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("jowhangBoardImgs", jowhangBoardImgDTOs);
        dataMap.put("dto", jowhangBoardDTO);
        return ResponseEntity.ok(dataMap);
    }

    @PostMapping("remove")
    public ResponseEntity<?> remove(@RequestBody JowhangBoardDTO jowhangBoardDTO) {

        jowhangBoardService.removeWithReplies(jowhangBoardDTO.getJbno());
        jowhangBoardImgService.remove(jowhangBoardDTO.getJbno());

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setSuccess(jowhangBoardDTO.getJbno() + "번이 삭제되었습니다.");

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("modify")
    public ResponseEntity<?> modify(JowhangBoardDTO dto,
                                    @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                    @RequestParam(value = "inos", required = false) List<Long> jinoList,
                                    @RequestParam(value = "deleteImages", required = false) List<Long> deleteImageIds) {

        if (jinoList == null || jinoList.isEmpty()) {
            if (images != null) {
                images.forEach(i -> {
                    jowhangBoardImgService.jowhangBoardRegister(i, dto.getJbno());
                });
            }
        } else {
            jowhangBoardImgService.updateImages(jinoList, images);
        }

        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            jowhangBoardImgService.deleteImages(deleteImageIds);
        }

        jowhangBoardService.modify(dto);

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setSuccess("수정되었습니다.");
        return ResponseEntity.ok(responseDTO);
    }

}

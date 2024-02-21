package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.*;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.service.NoticeBoardImgService;
import com.haegreen.fishing.service.NoticeBoardService;
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
@RequestMapping("noticeboard")
@Log4j2
@RequiredArgsConstructor
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;
    private final NoticeBoardImgService noticeBoardImgService;
    private final MemberRepository memberRepository;

    @GetMapping("list")
    public ResponseEntity<?> list(PageRequestDTO pageRequestDTO){

        PageResultDTO<NoticeBoardDTO, Object[]> pageResultDTO = noticeBoardService.getList(pageRequestDTO);
        if(pageResultDTO.getTotalPage()==0){ pageResultDTO.setTotalPage(1);} // 글이 하나도 없을 땐 0으로 인식하므로

        pageRequestDTO.setType(pageRequestDTO.getTypeAsString());

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("pageRequestDTO", pageRequestDTO);
        dataMap.put("result", pageResultDTO);

        return  ResponseEntity.ok(dataMap);
    }

    @PostMapping("register")
    public ResponseEntity<?> registerPost(NoticeBoardDTO dto, Authentication authentication, ResponseDTO responseDTO,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images){

        Member member = memberRepository.findByEmail(authentication.getName());

        dto.setWriterEmail(member.getEmail());

        Long nbno = noticeBoardService.register(dto); //새로 추가된 엔티티의 번호(dto)

        if(images != null) {
            images.forEach(i -> {
                noticeBoardImgService.noticeBoardRegister(i, nbno);
            });
        }

        responseDTO.setSuccess(nbno+"번이 등록되었습니다.");

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping({"read", "modify"})
    public ResponseEntity<?> read(@RequestParam("nbno") Long nbno){

        NoticeBoardDTO noticeBoardDTO = noticeBoardService.get(nbno);

        List<ImgDTO> noticeBoardImgDTOs = noticeBoardService.getImgList(nbno);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("noticeBoardImgs", noticeBoardImgDTOs);
        dataMap.put("dto", noticeBoardDTO);

        return ResponseEntity.ok(dataMap);
    }

    @PostMapping("remove")
    public ResponseEntity<?> remove(@RequestBody NoticeBoardDTO noticeBoardDTO){

        noticeBoardService.removeWithReplies(noticeBoardDTO.getNbno());
        noticeBoardImgService.remove(noticeBoardDTO.getNbno());

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setSuccess(noticeBoardDTO.getNbno() + "번이 삭제되었습니다.");

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("modify")
    public ResponseEntity<?> modify(NoticeBoardDTO dto,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @RequestParam(value = "inos", required = false) List<Long> ninoList,
                         @RequestParam(value = "deleteImages", required = false) List<Long> deleteImageIds){

        if(ninoList == null || ninoList.isEmpty()){
            if(images != null) {
                images.forEach(i -> {
                    noticeBoardImgService.noticeBoardRegister(i, dto.getNbno());
                });
            }
        }else {
            noticeBoardImgService.updateImages(ninoList, images);
        }

        if(deleteImageIds != null && !deleteImageIds.isEmpty()) {
            noticeBoardImgService.deleteImages(deleteImageIds);
        }

        noticeBoardService.modify(dto);

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setSuccess("수정되었습니다.");
        return ResponseEntity.ok(responseDTO);
    }

}

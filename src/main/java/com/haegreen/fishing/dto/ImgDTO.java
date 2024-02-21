package com.haegreen.fishing.dto;

import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.entitiy.NoticeBoard;
import lombok.*;

@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImgDTO {

    private Long ino; // 공통 이미지 번호
    private NoticeBoard noticeBoard;
    private JowhangBoard jowhangBoard;
    private String uuidfileName;
    private String realfileName;
}

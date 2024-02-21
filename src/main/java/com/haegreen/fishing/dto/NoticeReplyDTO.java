package com.haegreen.fishing.dto;


import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Data
public class NoticeReplyDTO {

    private Long nrno;
    private String text;
    private String replyer;
    private Long nbno;
    private LocalDateTime regDate, modDate;

}

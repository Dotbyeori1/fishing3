package com.haegreen.fishing.dto;


import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Data
public class JowhangReplyDTO {

    private Long jrno;
    private String text;
    private String replyer;
    private Long jbno;
    private LocalDateTime regDate, modDate;
    private boolean memberState;

}

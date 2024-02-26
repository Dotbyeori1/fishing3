package com.haegreen.fishing.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDateDTO {

    private Long rdate;

    private LocalDate regDate;
    private LocalDate startDate;
    private LocalDate endDate;

    private boolean available; // 예약 가능 여부 true : 예약 가능

    private String message;

    private String fishingSort; // 어업 종류

    private Integer extrasMembers; // 여유인원

    private Integer fishingMoney; // 금액

    private boolean dateModify; // 날짜 수정 여부


    private List<ReservationDTO> reservations;
}

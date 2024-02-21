package com.haegreen.fishing.dto;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
@AllArgsConstructor
@Setter
@Getter
@Data
public class PageRequestDTO {


    private int page;
    private int size;

    private String type; // 검색의 종류 t, c, tc, tw, twc

    private String keyword; // 검색어

    private String category; // 숙소종류

    private String region; // 지역

    public String[] getArrayType(){
        if(type == null || type.isEmpty()){
            return null;
        }
        return type.split("");
    }

    public String getTypeAsString(){
        if(type == null || type.isEmpty()){
            return null;
        }
        return String.join("", type.split(""));
    }

    public PageRequestDTO() {
        this.page = 1;
        this.size = 10;
    }

    public PageRequestDTO(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public Pageable getPageable(String...props) {
        return PageRequest.of(this.page -1, this.size, Sort.by(props).descending());
    }
}

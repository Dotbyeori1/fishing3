package com.haegreen.fishing.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Data
public class ResponseDTO {
    private String error;
    private String success;
    private List<?> dataList;
    private Object dataObject;
    private Map<String, Object> dataMap;
}

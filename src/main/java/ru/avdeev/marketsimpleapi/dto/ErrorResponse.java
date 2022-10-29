package ru.avdeev.marketsimpleapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private Integer status;
    private String message;
}

package ru.avdeev.marketsimpleapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private BigDecimal price;
}

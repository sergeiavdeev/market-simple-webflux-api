package ru.avdeev.marketsimpleapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPageResponse<T> {

    private Integer number;
    private Integer size;
    private Integer totalPages;
    private Long totalElements;
    private List<T> content;

    public ProductPageResponse(List<T> content, Long total, Integer pageNumber, Integer pageSize) {
        this.content = content;
        this.totalElements = total;
        this.number = pageNumber + 1;
        this.totalPages = pageSize == 0 ? 1 :  (int)Math.ceil((double)total / (double)pageSize);
        this.size = content.size();
    }
}

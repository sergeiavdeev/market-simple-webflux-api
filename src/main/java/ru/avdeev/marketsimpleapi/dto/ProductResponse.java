package ru.avdeev.marketsimpleapi.dto;

import lombok.Data;
import ru.avdeev.marketsimpleapi.entities.FileEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ProductResponse {

    private UUID id;
    private String title;
    private BigDecimal price;
    private List<FileEntity> files;
}

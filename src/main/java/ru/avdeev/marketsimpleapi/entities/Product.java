package ru.avdeev.marketsimpleapi.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Table(name = "product")
public class Product {

    @Id
    @Column("id")
    private UUID id;

    @Column("title")
    private String title;

    @Column("price")
    private BigDecimal price;

    @Transient
    private List<FileEntity> files;
}

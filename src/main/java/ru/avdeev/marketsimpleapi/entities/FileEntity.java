package ru.avdeev.marketsimpleapi.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table(name="file")
@AllArgsConstructor
@NoArgsConstructor
public class FileEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("owner_id")
    private UUID ownerId;

    @Column("name")
    private String name;

    @Column("order_num")
    private Integer order;

    @Column("descr")
    private String descr;
}

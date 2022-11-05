package ru.avdeev.marketsimpleapi.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table(name = "role")
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private User.UserRole role;

}

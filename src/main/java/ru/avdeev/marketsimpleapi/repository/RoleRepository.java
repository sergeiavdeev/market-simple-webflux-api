package ru.avdeev.marketsimpleapi.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.avdeev.marketsimpleapi.entities.Role;

import java.util.UUID;

public interface RoleRepository extends R2dbcRepository<Role, UUID> {

    @Query("SELECT ur.id as id, r.name as name FROM user_role ur LEFT JOIN role r on r.id = ur.role_id WHERE ur.user_id = :userId")
    Flux<Role> findByUserId(UUID userId);
}

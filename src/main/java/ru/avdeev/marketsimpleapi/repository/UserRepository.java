package ru.avdeev.marketsimpleapi.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.entities.User;

import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, UUID> {

    Mono<User> findByUsername(String name);
}

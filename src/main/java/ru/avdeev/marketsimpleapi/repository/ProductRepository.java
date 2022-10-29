package ru.avdeev.marketsimpleapi.repository;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.entities.Product;
import java.util.UUID;

public interface ProductRepository extends R2dbcRepository<Product, UUID> {

}

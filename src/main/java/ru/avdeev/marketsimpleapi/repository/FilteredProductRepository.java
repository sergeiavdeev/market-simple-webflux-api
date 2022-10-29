package ru.avdeev.marketsimpleapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Criteria;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.entities.Product;

public interface FilteredProductRepository {
    Mono<Page<Product>> getPage(Pageable page, Criteria criteria);
}

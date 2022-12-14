package ru.avdeev.marketsimpleapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Criteria;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.dto.ProductPageResponse;
import ru.avdeev.marketsimpleapi.dto.ProductResponse;

public interface FilteredProductRepository {
    Mono<ProductPageResponse<ProductResponse>> getPage(Pageable page, Criteria criteria);
}

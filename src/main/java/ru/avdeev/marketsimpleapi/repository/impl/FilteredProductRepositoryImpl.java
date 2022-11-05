package ru.avdeev.marketsimpleapi.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.dto.ProductPageResponse;
import ru.avdeev.marketsimpleapi.entities.Product;
import ru.avdeev.marketsimpleapi.repository.FilteredProductRepository;


@Repository
public class FilteredProductRepositoryImpl implements FilteredProductRepository {

    R2dbcEntityTemplate databaseClient;

    @Override
    @Transactional
    public Mono<ProductPageResponse<Product>> getPage(Pageable page, Criteria criteria) {

        Query query = Query.query(criteria)
                .sort(page.getSort())
                .limit(page.getPageSize())
                .offset((long) page.getPageSize() * page.getPageNumber());

        return databaseClient.select(Product.class).from("product")
                .matching(query)
                .all()
                .collectList()
                .zipWith(
                        databaseClient.select(Product.class)
                                .from("product")
                                .matching(query).count())
                .map(t -> new ProductPageResponse<>(t.getT1(), t.getT2(), page.getPageNumber(), page.getPageSize()));
    }

    @Autowired
    public void init(R2dbcEntityTemplate client) {
        databaseClient = client;
    }
}

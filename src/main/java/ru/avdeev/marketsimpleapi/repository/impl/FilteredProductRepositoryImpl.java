package ru.avdeev.marketsimpleapi.repository.impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.dto.ProductPageResponse;
import ru.avdeev.marketsimpleapi.dto.ProductResponse;
import ru.avdeev.marketsimpleapi.entities.Product;
import ru.avdeev.marketsimpleapi.mappers.ProductMapper;
import ru.avdeev.marketsimpleapi.repository.FileRepository;
import ru.avdeev.marketsimpleapi.repository.FilteredProductRepository;


@Repository
@AllArgsConstructor
public class FilteredProductRepositoryImpl implements FilteredProductRepository {

    private R2dbcEntityTemplate databaseClient;
    private FileRepository fileRepository;
    private ProductMapper mapper;

    @Override
    @Transactional
    public Mono<ProductPageResponse<ProductResponse>> getPage(Pageable page, Criteria criteria) {

        Query query = Query.query(criteria)
                .sort(page.getSort())
                .limit(page.getPageSize())
                .offset((long) page.getPageSize() * page.getPageNumber());

        return databaseClient.select(Product.class).from("product")
                .matching(query)
                .all()
                .map(mapper::mapToProductResponse)
                .flatMap(product -> fileRepository.findByOwnerIdOrderByOrder(product.getId())
                        .collectList()
                        .flatMap(files -> {
                            product.setFiles(files);
                            return Mono.just(product);
                        }))
                .collectList()
                .zipWith(
                        databaseClient.select(Product.class)
                                .from("product")
                                .matching(query).count())
                .map(t -> new ProductPageResponse<>(t.getT1(), t.getT2(), page.getPageNumber(), page.getPageSize()));
    }

}

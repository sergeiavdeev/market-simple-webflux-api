package ru.avdeev.marketsimpleapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.entities.Product;
import ru.avdeev.marketsimpleapi.exceptions.EntityNotFondException;
import ru.avdeev.marketsimpleapi.exceptions.InsertWithIdException;
import ru.avdeev.marketsimpleapi.repository.FilteredProductRepository;
import ru.avdeev.marketsimpleapi.repository.ProductRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ProductService {

    ProductRepository repository;
    FilteredProductRepository filteredRepository;

    public Mono<Page<Product>> getPage(Optional<String> page, Optional<String> size, Optional<String> title, Optional<String> minPrice, Optional<String> maxPrice, Optional<String> sort) {

        int pageNum = Integer.parseInt(page.orElse("1"));
        int pageSize = Integer.parseInt(size.orElse("5"));

        if (pageNum < 1)pageNum = 1;
        if (pageSize < 1)pageSize = 1;

        return filteredRepository.getPage(
                PageRequest.of(pageNum - 1, pageSize, sort.isPresent() ? createSortFromString(sort.get()) : Sort.unsorted()),
                creteCriteria(title, minPrice, maxPrice)
        );
    }

    public Mono<Product> getById(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFondException(id, "Product")));
    }

    @Transactional
    public Mono<Product> update(Product product) {
        return getById(product.getId())
                .flatMap(existProduct -> repository.save(product));
    }

    public Mono<Product> add(Product product) {
        if (product.getId() != null) {
            return Mono.error(new InsertWithIdException());
        }
        return repository.save(product);
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    @Autowired
    public void init(ProductRepository repository, FilteredProductRepository filteredRepository) {
        this.repository = repository;
        this.filteredRepository = filteredRepository;
    }

    private Sort createSortFromString(String sortString) {
        Sort sort = Sort.unsorted();
        String[] sortFields = sortString.split(",");
        for (String s : sortFields) {
            sort = sort.and(Sort.by(s));
        }
        return sort;
    }

    private Criteria creteCriteria(Optional<String> title, Optional<String> minPrice, Optional<String> maxPrice) {

        AtomicReference<Double> minPriceFilter = new AtomicReference<>();
        AtomicReference<Double> maxPriceFilter = new AtomicReference<>();

        minPrice.ifPresent(value -> minPriceFilter.set(Double.valueOf(value)));
        maxPrice.ifPresent(value -> maxPriceFilter.set(Double.valueOf(value)));

        Criteria criteria = Criteria.empty();

        if (title.isPresent())
            criteria = criteria.and(Criteria.where("title").like(String.format("%%%s%%", title.get())).ignoreCase(true));
        if (minPriceFilter.get() != null)
            criteria = criteria.and(Criteria.where("price").greaterThanOrEquals(minPriceFilter.get()));
        if (maxPriceFilter.get() != null)
            criteria = criteria.and(Criteria.where("price").lessThanOrEquals(maxPriceFilter.get()));

        return criteria;
    }
}

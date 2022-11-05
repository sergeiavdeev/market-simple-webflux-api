package ru.avdeev.marketsimpleapi.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.avdeev.marketsimpleapi.dto.ProductCreateRequest;
import ru.avdeev.marketsimpleapi.dto.ProductPageResponse;
import ru.avdeev.marketsimpleapi.entities.FileEntity;
import ru.avdeev.marketsimpleapi.entities.Product;
import ru.avdeev.marketsimpleapi.exceptions.EntityNotFondException;
import ru.avdeev.marketsimpleapi.mappers.ProductMapper;
import ru.avdeev.marketsimpleapi.repository.FileRepository;
import ru.avdeev.marketsimpleapi.repository.FilteredProductRepository;
import ru.avdeev.marketsimpleapi.repository.ProductRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class ProductService {

    ProductRepository repository;
    FileRepository fileRepository;
    FilteredProductRepository filteredRepository;
    ProductMapper mapper;
    @Value("${img.product.path}")
    private String basePath;

    @Value("${product.default-page-size}")
    private String defaultPageSize;

    public Mono<ProductPageResponse<Product>> getPage(Optional<String> page, Optional<String> size, Optional<String> title, Optional<String> minPrice, Optional<String> maxPrice, Optional<String> sort) {

        int pageNum = Integer.parseInt(page.orElse("1"));
        int pageSize = Integer.parseInt(size.orElse(defaultPageSize));

        if (pageNum < 1)pageNum = 1;
        if (pageSize < 1)pageSize = 1;

        return filteredRepository.getPage(
                PageRequest.of(pageNum - 1, pageSize, sort.isPresent() ? createSortFromString(sort.get()) : Sort.unsorted()),
                creteCriteria(title, minPrice, maxPrice)
        );
    }

    public Mono<Product> getById(UUID id) {
        return repository.findById(id)
                .flatMap(product -> fileRepository.findByOwnerIdOrderByOrder(product.getId())
                        .collectList()
                        .flatMap(fileEntities -> {
                            product.setFiles(fileEntities);
                            return Mono.just(product);
                        }))
                .switchIfEmpty(Mono.error(new EntityNotFondException(id, "Product")));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Product> update(Product product) {
        return getById(product.getId())
                .flatMap(existProduct -> repository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Product> add(ProductCreateRequest productCreateRequest) {

        return Mono.just(productCreateRequest)
                .map(mapper::mapToProduct)
                .flatMap(product -> repository.save(product));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id)
                .then(fileRepository.deleteByOwnerId(id))
                .then(productFilesDelete(id.toString()));
    }

    public Mono<Void> productFileSave(FilePart part, String productId) {

        Path filePath = Paths.get(basePath);
        String fileExtension = getFileExtension(part.filename());
        String newUUID = UUID.randomUUID().toString();
        String fileName = String.format("%s %s.%s", productId, newUUID, fileExtension);
        String dbFileName = String.format("%s.%s", newUUID, fileExtension);

        return part.transferTo(
                        filePath.resolve(fileName))
                .then(fileRepository.save(new FileEntity(
                        null,
                        UUID.fromString(productId),
                        dbFileName,
                        0, "")))
                .then();
    }

    public Mono<Void> productFileDelete(String id, String fileName) {

        Path path = Paths.get(basePath).resolve(id).resolve(fileName);
        return Mono.just(path)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(file -> {
                    new Thread(() -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            log.error("Can't delete file {}, error: {}", file, e.getMessage());
                        }
                    }).start();
                    return Mono.empty();
                });
    }

    public Mono<Void> productFilesDelete(String id) {

        Path path = Paths.get(basePath).resolve(id);
        return Mono.just(path)
                .flatMap(directory -> {
                    new Thread(() -> {
                        try {
                            File[] allContents = directory.toFile().listFiles();
                            if (allContents != null) {
                                for (File file : allContents) {
                                    Files.delete(file.toPath());
                                }
                            }
                            Files.delete(directory);
                        } catch (IOException e) {
                            log.error("Can't delete file or directory {}, error: {}", directory, e.getMessage());
                        }
                    }).start();
                    return Mono.empty();
                });
    }



    @Autowired
    public void init(ProductRepository repository,
                     FilteredProductRepository filteredRepository,
                     ProductMapper m,
                     FileRepository fr) {
        this.repository = repository;
        this.filteredRepository = filteredRepository;
        mapper = m;
        fileRepository = fr;
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

    private String getFileExtension(String fileName) {

        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}

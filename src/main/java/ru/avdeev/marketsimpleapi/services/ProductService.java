package ru.avdeev.marketsimpleapi.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.dto.ProductCreateRequest;
import ru.avdeev.marketsimpleapi.dto.ProductPageResponse;
import ru.avdeev.marketsimpleapi.dto.ProductResponse;
import ru.avdeev.marketsimpleapi.entities.FileEntity;
import ru.avdeev.marketsimpleapi.entities.Product;
import ru.avdeev.marketsimpleapi.exceptions.EntityNotFondException;
import ru.avdeev.marketsimpleapi.mappers.ProductMapper;
import ru.avdeev.marketsimpleapi.repository.FileCloudRepository;
import ru.avdeev.marketsimpleapi.repository.FileRepository;
import ru.avdeev.marketsimpleapi.repository.FilteredProductRepository;
import ru.avdeev.marketsimpleapi.repository.ProductRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class ProductService {

    ProductRepository repository;
    FileRepository fileRepository;
    FilteredProductRepository filteredRepository;

    FileCloudRepository fileCloudRepository;
    ProductMapper mapper;

    @Value("${product.default-page-size}")
    private String defaultPageSize;

    public Mono<ProductPageResponse<ProductResponse>> getPage(Optional<String> page, Optional<String> size, Optional<String> title, Optional<String> minPrice, Optional<String> maxPrice, Optional<String> sort) {

        int pageNum = Integer.parseInt(page.orElse("1"));
        int pageSize = Integer.parseInt(size.orElse(defaultPageSize));

        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 1;

        return filteredRepository.getPage(
                PageRequest.of(pageNum - 1, pageSize, sort.isPresent() ? createSortFromString(sort.get()) : Sort.unsorted()),
                creteCriteria(title, minPrice, maxPrice)
        );
    }

    public Mono<ProductResponse> getById(UUID id) {
        return repository.findById(id)
                .map(mapper::mapToProductResponse)
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
                .then(fileCloudRepository.deleteFolder(id.toString()));
    }

    public Mono<FileEntity> saveFile(FilePart part, String productId, Optional<Integer> order, Optional<String> descr) {

        String fileExtension = getFileExtension(part.filename());
        String newUUID = UUID.randomUUID().toString();
        String fileName = String.format("%s.%s", newUUID, fileExtension);
        String dbFileName = String.format("%s.%s", newUUID, fileExtension);

        return fileCloudRepository.save(productId, fileName, part)
                .flatMap(success -> fileRepository.save(new FileEntity(
                        null,
                        UUID.fromString(productId),
                        dbFileName,
                        order.orElse(0), descr.orElse("")))
                )
                .onErrorResume(Mono::error);
    }

    public Mono<Void> fileDelete(UUID id) {
        return fileRepository.findById(id)
                .flatMap(fileEntity -> fileRepository.deleteById(id)
                        .then(fileCloudRepository.delete(fileEntity.getOwnerId().toString(), fileEntity.getName())));
    }

    @Autowired
    public void init(ProductRepository repository,
                     FilteredProductRepository filteredRepository,
                     ProductMapper m,
                     FileRepository fr,
                     FileCloudRepository fc) {
        this.repository = repository;
        this.filteredRepository = filteredRepository;
        mapper = m;
        fileRepository = fr;
        fileCloudRepository = fc;
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

        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }
}

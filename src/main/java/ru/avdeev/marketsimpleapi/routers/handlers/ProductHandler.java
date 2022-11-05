package ru.avdeev.marketsimpleapi.routers.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.dto.ProductCreateRequest;
import ru.avdeev.marketsimpleapi.dto.ProductPageResponse;
import ru.avdeev.marketsimpleapi.entities.Product;
import ru.avdeev.marketsimpleapi.services.FileService;
import ru.avdeev.marketsimpleapi.services.ProductService;

import java.util.UUID;

@Component
@Slf4j
public class ProductHandler {

    ProductService productService;
    FileService fileService;
    public Mono<ServerResponse> get(ServerRequest request) {

        return ServerResponse.ok()
                .body(productService.getPage(
                        request.queryParam("page"),
                        request.queryParam("size"),
                        request.queryParam("title"),
                        request.queryParam("minPrice"),
                        request.queryParam("maxPrice"),
                        request.queryParam("sort")
                ), ProductPageResponse.class);

    }

    public Mono<ServerResponse> getById(ServerRequest request) {

        UUID id = UUID.fromString(request.pathVariable("id"));
        return productService.getById(id)
                .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        return request.bodyToMono(ProductCreateRequest.class)
                .flatMap(product -> productService.add(product))
                .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }

    public Mono<ServerResponse> update(ServerRequest request) {

        return request.bodyToMono(Product.class).log()
                .flatMap(product -> productService.update(product))
                .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {

        return ServerResponse.ok()
                .body(productService.delete(UUID.fromString(request.pathVariable("id"))), Void.class);
    }

    public Mono<ServerResponse> fileUpload(ServerRequest request) {

        return request.multipartData()
                .flatMap(parts ->
                        productService.productFileSave((FilePart)parts.toSingleValueMap().get("file"), request.pathVariable("id")))
                .then(ServerResponse.ok().build());
    }



    @Autowired
    public void init(ProductService ps, FileService fs) {
        this.productService = ps;
        this.fileService = fs;
    }
}

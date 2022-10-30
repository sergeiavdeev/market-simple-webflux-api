package ru.avdeev.marketsimpleapi.routers.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.dto.ProductPageResponse;
import ru.avdeev.marketsimpleapi.entities.Product;
import ru.avdeev.marketsimpleapi.services.ProductService;

import java.util.UUID;

@Component
public class ProductHandler {

    ProductService service;

    public Mono<ServerResponse> get(ServerRequest request) {

        return ServerResponse.ok()
                .body(service.getPage(
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
        return service.getById(id)
                .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        return request.bodyToMono(Product.class)
                .flatMap(product -> service.add(product))
                .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }

    public Mono<ServerResponse> update(ServerRequest request) {

        return request.bodyToMono(Product.class).log()
                .flatMap(product -> service.update(product))
                .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {

        return ServerResponse.ok()
                .body(service.delete(UUID.fromString(request.pathVariable("id"))), Void.class);
    }

    @Autowired
    public void init(ProductService service) {
        this.service = service;
    }
}

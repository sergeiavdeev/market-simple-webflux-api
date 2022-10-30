package ru.avdeev.marketsimpleapi.routers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebInputException;
import ru.avdeev.marketsimpleapi.dto.ProductPageResponse;
import ru.avdeev.marketsimpleapi.entities.Product;
import ru.avdeev.marketsimpleapi.exceptions.ApiException;
import ru.avdeev.marketsimpleapi.dto.ErrorResponse;
import ru.avdeev.marketsimpleapi.routers.handlers.HelloHandler;
import ru.avdeev.marketsimpleapi.routers.handlers.ProductHandler;
import ru.avdeev.marketsimpleapi.services.ProductService;

import java.util.UUID;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
public class Router implements WebFluxConfigurer {

    @Bean
    @RouterOperations ({
            @RouterOperation(path = "/api/v1/product",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    beanClass = ProductService.class,
                    method = RequestMethod.GET,
                    beanMethod = "getPage",
                    operation = @Operation(operationId = "getPage",
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = ProductPageResponse.class))
                                    )
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.QUERY, name = "page", schema = @Schema(implementation = Integer.class)),
                                    @Parameter(in = ParameterIn.QUERY, name = "size", schema = @Schema(implementation = Integer.class)),
                                    @Parameter(in = ParameterIn.QUERY, name = "title"),
                                    @Parameter(in = ParameterIn.QUERY, name = "minPrice", schema = @Schema(implementation = Double.class)),
                                    @Parameter(in = ParameterIn.QUERY, name = "maxPrice", schema = @Schema(implementation = Double.class))
                            }
                    )
            ),
            @RouterOperation(path = "/api/v1/product/{id}",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    beanClass = ProductService.class,
                    method = RequestMethod.GET,
                    beanMethod = "getById",
                    operation = @Operation(operationId = "getById",
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = Product.class))
                                    )
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(implementation = UUID.class))
                            }
                    )
            ),
            @RouterOperation(path = "/api/v1/product",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    beanClass = ProductService.class,
                    method = RequestMethod.POST,
                    beanMethod = "add",
                    operation = @Operation(operationId = "add",
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = Product.class))
                                    ),
                                    @ApiResponse(responseCode = "400",
                                            description = "bad request",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                                    )
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = Product.class)))
                    )
            ),
            @RouterOperation(path = "/api/v1/product",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    beanClass = ProductService.class,
                    method = RequestMethod.PUT,
                    beanMethod = "update",
                    operation = @Operation(operationId = "update",
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = Product.class))
                                    ),
                                    @ApiResponse(responseCode = "404",
                                            description = "not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                                    ),
                                    @ApiResponse(responseCode = "400",
                                            description = "bad request",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                                    )
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = Product.class)))
                    )
            ),
            @RouterOperation(path = "/api/v1/product/{id}",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    beanClass = ProductService.class,
                    method = RequestMethod.DELETE,
                    beanMethod = "delete",
                    operation = @Operation(operationId = "delete",
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation"
                                    )
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(implementation = UUID.class))
                            }
                    )
            )

    })
    public RouterFunction<ServerResponse> productRouter(ProductHandler handler) {

        return route()
                .path("/api/v1/product", b-> b
                        .GET("", handler::get)
                        .GET("/{id}", handler::getById)
                        .POST("", handler::add)
                        .PUT("", handler::update)
                        .DELETE("/{id}", handler::delete)
                        .filter(apiExceptionHandler())
                ).build();
    }

    @Bean
    @RouterOperations ({
            @RouterOperation(path = "/api/v1/hello",
                    produces = {MediaType.TEXT_PLAIN_VALUE},
                    beanClass = HelloHandler.class,
                    method = RequestMethod.GET,
                    beanMethod = "hello",
                    operation = @Operation(operationId = "hello",
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = String.class))
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> helloRouter(HelloHandler handler) {

        return route()
                .path("/api/v1/hello", b-> b
                        .GET("", handler::hello)
                ).build();
    }

    private HandlerFilterFunction<ServerResponse, ServerResponse> apiExceptionHandler() {

        return (request, next) -> next.handle(request).log()
                .onErrorResume(ApiException.class,
                        e -> ServerResponse
                                .status(e.getStatus())
                                .bodyValue(new ErrorResponse(e.getStatus().value(), e.getMessage()))
                )
                .onErrorResume(ServerWebInputException.class,
                        e -> ServerResponse
                                .status(HttpStatus.BAD_REQUEST)
                                .bodyValue(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()))
                );
    }
}

package ru.avdeev.marketsimpleapi.routers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebInputException;
import ru.avdeev.marketsimpleapi.dto.*;
import ru.avdeev.marketsimpleapi.entities.Product;
import ru.avdeev.marketsimpleapi.exceptions.ApiException;
import ru.avdeev.marketsimpleapi.routers.handlers.AuthHandler;
import ru.avdeev.marketsimpleapi.routers.handlers.HelloHandler;
import ru.avdeev.marketsimpleapi.routers.handlers.ProductHandler;

import java.math.BigDecimal;

import java.util.UUID;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
public class Router implements WebFluxConfigurer {

    @Bean
    @RouterOperations ({
            @RouterOperation(path = "/api/v1/product",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    beanClass = ProductHandler.class,
                    method = RequestMethod.GET,
                    beanMethod = "get",
                    operation = @Operation(operationId = "get", description = "Get pageable product list",
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = ProductPageResponse.class))
                                    )
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.QUERY, name = "page", schema = @Schema(implementation = Integer.class)),
                                    @Parameter(in = ParameterIn.QUERY, name = "size", schema = @Schema(implementation = Integer.class)),
                                    @Parameter(in = ParameterIn.QUERY, name = "title", schema = @Schema(implementation = String.class)),
                                    @Parameter(in = ParameterIn.QUERY, name = "minPrice", schema = @Schema(implementation = BigDecimal.class)),
                                    @Parameter(in = ParameterIn.QUERY, name = "maxPrice", schema = @Schema(implementation = BigDecimal.class)),
                                    @Parameter(in = ParameterIn.QUERY, name = "sort", schema = @Schema(implementation = String.class))
                            }
                    )
            ),
            @RouterOperation(path = "/api/v1/product/{id}",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    beanClass = ProductHandler.class,
                    method = RequestMethod.GET,
                    beanMethod = "getById",
                    operation = @Operation(operationId = "getById",  description = "Get product by Id",
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
                    beanClass = ProductHandler.class,
                    method = RequestMethod.POST,
                    beanMethod = "add",
                    operation = @Operation(operationId = "add", description = "Create new product",
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
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ProductCreateRequest.class))),
                            security = @SecurityRequirement(name = "jwt")
                    )
            ),
            @RouterOperation(path = "/api/v1/product",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    beanClass = ProductHandler.class,
                    method = RequestMethod.PUT,
                    beanMethod = "update",
                    operation = @Operation(operationId = "update",  description = "Update existing product",
                            security = @SecurityRequirement(name = "jwt"),
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
                    beanClass = ProductHandler.class,
                    method = RequestMethod.DELETE,
                    beanMethod = "delete",
                    operation = @Operation(operationId = "delete",  description = "Delete product",
                            security = @SecurityRequirement(name = "jwt"),
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema())
                                    )
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(implementation = UUID.class))
                            }
                    )
            ),
            @RouterOperation(path = "/api/v1/product/{id}/file",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = "multipart/form-data",
                    beanClass = ProductHandler.class,
                    method = RequestMethod.POST,
                    beanMethod = "fileUpload",
                    operation = @Operation(operationId = "fileUpload",  description = "Upload file for existing product",
                            //security = @SecurityRequirement(name = "jwt"),
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema())
                                    )
                            },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(implementation = UUID.class))
                            },
                            requestBody = @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schemaProperties = {
                                    @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")),
                                    @SchemaProperty(name = "order", schema = @Schema(type = "string", format = "string")),
                                    @SchemaProperty(name = "descr", schema = @Schema(type = "string", format = "string"))
                            }))
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
                        .POST("/{id}/file", handler::fileUpload)
                        .POST("/file/{id}", handler::fileDelete)
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

    @Bean
    @RouterOperations ({
            @RouterOperation(path = "/api/v1/auth",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    beanClass = AuthHandler.class,
                    method = RequestMethod.POST,
                    beanMethod = "auth",
                    operation = @Operation(operationId = "auth",
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                                    ),
                                    @ApiResponse(responseCode = "401",
                                            description = "not authorized",
                                            content = @Content(schema = @Schema())
                                    )
                            },
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = AuthRequest.class)))
                    )
            )
    })
    public RouterFunction<ServerResponse> authRouter(AuthHandler handler) {

        return route()
                .path("/api/v1/auth", b -> b
                        .POST("", handler::auth)
                ).build();
    }

    @Bean
    public RouterFunction<ServerResponse> staticRouter() {
        return RouterFunctions
                .resources("/**", new ClassPathResource("static/"))
                ;
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

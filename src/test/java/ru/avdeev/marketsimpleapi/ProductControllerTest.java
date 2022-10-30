package ru.avdeev.marketsimpleapi;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.avdeev.marketsimpleapi.dto.ProductPageResponse;
import ru.avdeev.marketsimpleapi.entities.Product;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;



@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductControllerTest {

    @Autowired
    WebTestClient client;

    private final String apiUrl = "/api/v1/product";
    private static Product createdProduct;

    @Test
    @Order(1)
    public void createProduct() {

        createdProduct = new Product();
        createdProduct.setTitle("Тест");
        createdProduct.setPrice(600d);

        client.post().uri(apiUrl)
                .body(BodyInserters.fromValue(createdProduct))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class).value(product -> createdProduct.setId(product.getId()));
    }

    @Test
    @Order(5)
    public void testGetPage() {
        client.get().uri(apiUrl)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductPageResponse.class).value(page -> {
                    assertThat(page.getSize()).isEqualTo(5);
                    assertThat(page.getNumber()).isEqualTo(0);
                });
    }

    @Test
    @Order(5)
    public void testGetById() {
        client.get().uri(uriBuilder -> uriBuilder
                        .path(apiUrl + "/{id}")
                        .build(createdProduct.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class).value(product -> assertThat(product.getId())
                        .isEqualTo(createdProduct.getId()));
    }

    @Test
    @Order(5)
    public void testUpdate() {

        createdProduct.setPrice(700d);
        client.put().uri(apiUrl)
                .body(BodyInserters.fromValue(createdProduct))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class).value(product -> assertThat(product.getPrice())
                        .isEqualTo(700d));
    }

    @Test
    @Order(10)
    public void deleteProduct() {

        client.delete().uri(uriBuilder -> uriBuilder
                        .path(apiUrl + "/{id}")
                        .build(createdProduct.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}

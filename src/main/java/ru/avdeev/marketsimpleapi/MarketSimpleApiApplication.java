package ru.avdeev.marketsimpleapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Market Simple API",
				version = "1.0",
				description = "Market Simple API reactive. Version 1.0"
		)
)
public class MarketSimpleApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketSimpleApiApplication.class, args);
	}

}

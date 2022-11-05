package ru.avdeev.marketsimpleapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
		info = @Info(
				title = "Market Simple API",
				version = "1.0",
				description = "Market Simple API reactive. Version 1.0"
		)
)
@SecurityScheme(name = "jwt", scheme = "Bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class MarketSimpleApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketSimpleApiApplication.class, args);
	}

}

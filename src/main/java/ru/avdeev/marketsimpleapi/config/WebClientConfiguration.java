package ru.avdeev.marketsimpleapi.config;

import brave.http.HttpTracing;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.brave.ReactorNettyHttpTracing;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient webClient(WebClient.Builder builder, ReactorNettyHttpTracing tracing) {
        return builder.clientConnector(
                new ReactorClientHttpConnector(
                        tracing.decorateHttpClient(
                                HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                                .doOnConnected(conn -> conn
                                        .addHandlerLast(new ReadTimeoutHandler(1000))
                                        .addHandlerLast(new WriteTimeoutHandler(1000))
                                )
                        )
                )
        )
                .codecs(clientCodecConfigurer -> clientCodecConfigurer
                        .defaultCodecs()
                        .maxInMemorySize(100 * 1024))
                .build();
    }

    @Bean
    public ReactorNettyHttpTracing reactorNettyHttpTracing(HttpTracing tracing) {
        return ReactorNettyHttpTracing.create(tracing);
    }


}

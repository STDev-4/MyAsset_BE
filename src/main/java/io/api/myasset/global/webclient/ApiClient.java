package io.api.myasset.global.webclient;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class ApiClient {

    protected final WebClient webClient;

    protected <T> T post(
            Function<UriBuilder, URI> uriFunction,
            Object body,
            Class<T> responseType
    ) {
        return webClient.post()
                .uri(uriFunction)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    protected <T> T get(
            Function<UriBuilder, URI> uriFunction,
            Class<T> responseType
    ) {
        return webClient.get()
                .uri(uriFunction)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    protected <T> T get(
            Function<UriBuilder, URI> uriFunction,
            Class<T> responseType,
            Consumer<HttpHeaders> headersCustomizer
    ) {
        WebClient.RequestHeadersSpec<?> spec = webClient.get()
                .uri(uriFunction);

        spec.headers(headersCustomizer);

        return spec.retrieve()
                .bodyToMono(responseType)
                .block();
    }
}
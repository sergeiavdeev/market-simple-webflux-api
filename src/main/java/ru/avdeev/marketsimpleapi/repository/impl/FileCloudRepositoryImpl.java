package ru.avdeev.marketsimpleapi.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Repository;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.exceptions.FileCloudException;
import ru.avdeev.marketsimpleapi.repository.FileCloudRepository;

@Repository
@Slf4j
public class FileCloudRepositoryImpl implements FileCloudRepository {

    @Value("${cloud.dir.product}")
    private String baseDir;

    @Value("${cloud.url}")
    private String url;

    @Value("${cloud.upload}")
    private String upload;

    @Value("${cloud.delete}")
    private String delete;

    private final WebClient webClient;

    public FileCloudRepositoryImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Boolean> save(String folder, String filename, FilePart filePart) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", filePart);
        MultiValueMap<String, HttpEntity<?>> parts = builder.build();
        String filePath = baseDir + folder;

        return webClient.post()
                .uri(urlBuilder -> urlBuilder
                        .path(url)
                        .pathSegment(upload)
                        .queryParam("folder", filePath)
                        .queryParam("name", filename)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .exchangeToMono(response -> {
                    if (response.statusCode() != HttpStatus.OK)
                        return Mono.error(new FileCloudException("Cant' save file into cloud"));
                    log.info("File saved to cloud");
                    return Mono.just(Boolean.TRUE);
                });
    }

    @Override
    public Mono<Void> delete(String folder, String filename) {
        return webClient.post()
                .uri(b -> b
                        .path(url)
                        .pathSegment(delete)
                        .queryParam("folder", folder)
                        .queryParam("name", filename)
                        .build())
                .exchangeToMono(response -> {
                    if (response.statusCode() == HttpStatus.OK) {
                        log.info("File delete from cloud: {}", filename);
                        return Mono.empty();
                    }
                    log.info("Delete file failing: {}", filename);
                    return Mono.error(new FileCloudException("Delete file failing: " + filename));
                });
    }

    @Override
    public Mono<Void> deleteFolder(String folder) {
        return webClient.post()
                .uri(b -> b
                        .path(url)
                        .pathSegment(delete)
                        .queryParam("folder", folder)
                        .build())
                .exchangeToMono(response -> {
                    if (response.statusCode() == HttpStatus.OK) {
                        log.info("Directory delete from cloud: {}", folder);
                        return Mono.empty();
                    }
                    log.info("Delete directory failing: {}", folder);
                    return Mono.error(new FileCloudException("Delete directory failing: " + folder));
                });
    }
}

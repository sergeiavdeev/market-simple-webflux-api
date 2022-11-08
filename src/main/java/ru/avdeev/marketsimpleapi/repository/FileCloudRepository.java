package ru.avdeev.marketsimpleapi.repository;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileCloudRepository {
    Mono<Boolean> save(String folder, String filename, FilePart filePart);
    Mono<Void> delete(String folder, String filename);
    Mono<Void> deleteFolder(String folder);
}

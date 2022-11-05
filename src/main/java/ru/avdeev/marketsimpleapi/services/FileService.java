package ru.avdeev.marketsimpleapi.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.avdeev.marketsimpleapi.repository.FileRepository;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Service
@Slf4j
public class FileService {

    FileRepository repository;
    @Value("${img.product.path}")
    private String basePath;

    @Scheduled(cron = "*/60 * * * * *")
    public void moveFiles() {
        log.info("Moving files task started at ");
        Path path = Paths.get(basePath);
        for(File file : Objects.requireNonNull(path.toFile().listFiles())) {
            if (file.isFile()) {
                String[] nameParts = file.getName().split(" ");
                if (nameParts.length > 1) {
                    try {
                        Path destinationDir = Paths.get(basePath).resolve(nameParts[0]);
                        if (!Files.exists(destinationDir)) {
                            Files.createDirectories(destinationDir);
                        }
                        Files.move(file.toPath(), destinationDir.resolve(nameParts[1]));
                        log.info("File {} moving to {}", nameParts[1], destinationDir);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        log.info("Moving files task end");
    }

    @Autowired
    public void init(FileRepository fr) {
        repository = fr;
    }
}

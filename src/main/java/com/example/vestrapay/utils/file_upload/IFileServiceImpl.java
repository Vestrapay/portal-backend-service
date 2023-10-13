package com.example.vestrapay.utils.file_upload;

import com.example.vestrapay.notifications.models.EmailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
@Slf4j
public class IFileServiceImpl implements IFileService {
    private final Path root = Paths.get("uploads");

    @Override
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }

    }

    @Override
    public void save(String merchantId,MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(merchantId+file.getOriginalFilename()));
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }

            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());

    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    public Mono<Void> doUpload(String merchantId,MultipartFile[] fileList) {
        for (MultipartFile multipartFile:fileList) {
            save(merchantId,multipartFile);
        }
        return null;
    }

    @Override
    public Mono<Boolean> upload(String merchantId,MultipartFile[] fileList) {
        Mono<Boolean> processMono = Mono.fromCallable(() -> {
            doUpload(merchantId,fileList).subscribe();
            return true;
        });
        processMono.subscribeOn(Schedulers.boundedElastic()).subscribe();
        return Mono.empty();
    }
}

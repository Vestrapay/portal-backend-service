package com.example.vestrapay.utils.file_upload;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface IFileService {
    public void init();

    public void save(String merchantId,MultipartFile file);

    public Resource load(String filename);

    public void deleteAll();

    public Stream<Path> loadAll();
    Mono<Boolean> upload(String merchantId,MultipartFile[] fileList);
}

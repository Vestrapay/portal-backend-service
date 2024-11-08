package com.example.vestrapay.merchant.dispute.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

public interface IDisputeFileService {
    void init();
    void save(String merchantId, String fileName, MultipartFile file);
    String saveOne(String merchantId,MultipartFile file);

    Resource load(String filename);
    void deleteAll();

    Stream<Path> loadAll();

    byte[] loadAll(String transactionReference);

    Mono<Set<String>> singleUpload(String transactionID, MultipartFile[] files);
}

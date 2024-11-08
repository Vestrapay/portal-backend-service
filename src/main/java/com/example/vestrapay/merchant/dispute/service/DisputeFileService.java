package com.example.vestrapay.merchant.dispute.service;

import com.example.vestrapay.merchant.dispute.interfaces.IDisputeFileService;
import com.example.vestrapay.merchant.users.repository.UserRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class DisputeFileService implements IDisputeFileService {
    private final Path root = Paths.get("dispute_uploads");
    private final UserRepository userRepository;
    private final Gson gson;

    @Override
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }

    }

    @Override
    public void save(String merchantId,String fileName,MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve((merchantId+fileName+file.getOriginalFilename()).replace(" ","_")));
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }

            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String saveOne(String transactionID,MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve((transactionID+"_"+file.getOriginalFilename()).replace(" ","_")));
            return transactionID+file.getOriginalFilename();
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

    @Override
    public byte[] loadAll(String transactionReference) {
        try(Stream<Path>paths= Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize)) {
            List<String> list = paths.filter(path -> path.getFileName().toString().startsWith(transactionReference)).map(path -> path.getFileName().toString()).toList();

            return createZip(list);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Set<String>> singleUpload(String transactionID,MultipartFile[] files){
        Set<String> fileList = new HashSet<>();
        for (MultipartFile file:files) {
            String path = saveOne(transactionID, file);
            fileList.add(path);

        }
        return Mono.just(fileList);


    }

    private byte[] createZip(List<String> fileNames) throws IOException {
        File zipFile = File.createTempFile("files", ".zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new java.io.FileOutputStream(zipFile))) {
            for (String fileName : fileNames) {
                Path filePath = root.resolve(fileName);
                File file = filePath.toFile();

                zipOutputStream.putNextEntry(new ZipEntry(fileName));
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fileInputStream.read(buffer)) > 0) {
                        zipOutputStream.write(buffer, 0, length);
                    }
                }
                zipOutputStream.closeEntry();
            }
        }
        return readBytesFromFile(zipFile);
    }

    private byte[] readBytesFromFile(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            return bytes;
        }
    }
}

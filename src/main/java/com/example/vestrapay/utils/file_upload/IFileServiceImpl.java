package com.example.vestrapay.utils.file_upload;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.users.repository.UserRepository;
import com.example.vestrapay.utils.dtos.Response;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Component("FileService")
@Slf4j
@RequiredArgsConstructor
public class IFileServiceImpl{
    private final Path root = Paths.get("uploads");
    private final UserRepository userRepository;
    private final Gson gson;

    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }

    }

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

    public String saveOne(String merchantId,MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve((merchantId+file.getOriginalFilename()).replace(" ","_")));
            return merchantId+file.getOriginalFilename();
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }

            throw new RuntimeException(e.getMessage());
        }
    }

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

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());

    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    public Mono<Void> doUpload(String merchantId, Map<String, MultipartFile[]> fileList) {
        Set<String> requiredDocuments = new HashSet<>(Set.of("register_of_shareholder",
                "certificate_of_incorporation",
                "register_of_directors",
                "memorandum_and_articles_of_association",
                "valid_id_of_directors",
                "valid_id_of_ultimate_beneficial_owners",
                "operating_license",
                "due_diligence_questionaire"));
        Set<String> uploadedDocuments = new HashSet<>();
        for (String key: requiredDocuments) {
            if (fileList.containsKey(key)){
                MultipartFile[] multipartFiles = fileList.get(key);
                for (MultipartFile multipartFile:multipartFiles) {
                    save(merchantId,key,multipartFile);
                }
                uploadedDocuments.add(key);
            }
        }
        return userRepository.findByMerchantId(merchantId)
                .collectList()
                .flatMap(users -> {
                    List<User> userList = new ArrayList<>();
                    for (User user1:users) {
                        String previouslyUploaded = user1.getRequiredDocuments();
                        if(Objects.nonNull(previouslyUploaded)){
                            Set set = gson.fromJson(previouslyUploaded, Set.class);
                            uploadedDocuments.addAll(set);
                        }

                        user1.setRequiredDocuments(uploadedDocuments.toString());
                        userList.add(user1);
                    }

                    return userRepository.saveAll(userList)
                            .collectList()
                            .flatMap(users1 -> {
                                log.info("kyc for merchant {} successfully updated",merchantId);
                                return Mono.just(Response.<List<User>>builder()
                                        .data(users1)
                                        .status(HttpStatus.OK)
                                        .statusCode(HttpStatus.OK.value())
                                        .message(SUCCESSFUL)
                                        .build());
                            }).doOnError(throwable -> {
                                log.error("error updating kyc for merchant and merchant users");
                                throw new CustomException(Response.builder()
                                        .message(FAILED)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of("Error updating KYC status",throwable.getLocalizedMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });

                }).then();
    }

    public Mono<Boolean> upload(String merchantId, Map<String, MultipartFile[]> fileList) {
        Mono<Boolean> processMono = Mono.fromCallable(() -> {
            doUpload(merchantId,fileList).subscribe();
            return true;
        });
        processMono.subscribeOn(Schedulers.boundedElastic()).subscribe();
        return Mono.empty();
    }

    public Mono<Set<String>> singleUpload(String merchantId,MultipartFile[] files){
        Set<String> fileList = new HashSet<>();
        for (MultipartFile file:files) {
            String path = saveOne(merchantId, file);
            fileList.add(path);

        }
        return Mono.just(fileList);


    }
}

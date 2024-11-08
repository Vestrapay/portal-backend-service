package com.example.vestrapay.utils.file_upload;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.merchant.kyc.models.Document;
import com.example.vestrapay.merchant.kyc.repositories.DocumentRepository;
import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.merchant.users.repository.UserRepository;
import com.example.vestrapay.utils.dtos.Response;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileService implements IFileService
{
    private final AmazonS3 s3client;
    private final UserRepository userRepository;
    @Value("${aws.s3.bucketName}")
    private String bucketName;
    @Value("${required.documents}")
    private Set<String>requiredDocuments;
    private final Gson gson;

    private final DocumentRepository documentRepository;


    @Override
    public Mono<Response<List<Document>>> loadAll(String merchantId) {
        return documentRepository.findByMerchantId(merchantId)
                .collectList()
                .flatMap(documents -> Mono.just(Response.<List<Document>>builder()
                                .data(documents)
                                .statusCode(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("SUCCESS")
                        .build()));
    }

    @Override
    public Mono<Boolean> upload(String merchantId, Map<String, MultipartFile[]> fileList) {

        Mono<Boolean> processMono = Mono.fromCallable(() -> {
            doUpload(merchantId,fileList).subscribe();
            return true;
        });
        processMono.subscribeOn(Schedulers.boundedElastic()).subscribe();
        return Mono.empty();
    }

    @Override
    public Mono<String> disputeUpload(String merchantId, MultipartFile file) {
        return Mono.just(saveDispute(merchantId,file));

    }

    private String saveDispute(String fileName, MultipartFile file) {
        String key = file.getOriginalFilename();

        try {
            //we would need to have a key-generation-id +filename

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            PutObjectRequest request = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata);
            PutObjectRequest putObjectRequest = request.withCannedAcl(CannedAccessControlList.PublicReadWrite);

            s3client.putObject(putObjectRequest);
            s3client.setObjectAcl(bucketName,key,CannedAccessControlList.PublicReadWrite);
        }
        catch (Exception e){
            log.warn("method name :: uploadFileToS3 error is {}",e.getMessage());
        }
        return s3client.getUrl(bucketName, key).toString();

    }

    private void save(String merchantId, String fileName, MultipartFile file) {
        try {
            //we would need to have a key-generation-id +filename
            String key = file.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            PutObjectRequest request = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata);
//            PutObjectRequest putObjectRequest = request.withCannedAcl(CannedAccessControlList.PublicReadWrite);

            s3client.putObject(request);
//            s3client.setObjectAcl(bucketName,key,CannedAccessControlList.PublicReadWrite);
            String fileUrl = s3client.getUrl(bucketName, key).toString();
            documentRepository.save(Document.builder()
                            .documentName(fileName)
                            .merchantId(merchantId)
                            .file_url(fileUrl)
                            .documentId(UUID.randomUUID().toString())
                    .build()).subscribe();
        }
        catch (Exception e){
            log.warn("method name :: uploadFileToS3 error is {}",e.getMessage());
        }
    }

    private Mono<Void> doUpload(String merchantId, Map<String, MultipartFile[]> fileList) {
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


}

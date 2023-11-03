package com.example.vestrapay.kyc.services;

import com.example.vestrapay.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.kyc.dtos.UpdateKycDTO;
import com.example.vestrapay.kyc.interfaces.IKycService;
import com.example.vestrapay.notifications.models.EmailDTO;
import com.example.vestrapay.notifications.services.NotificationService;
import com.example.vestrapay.users.enums.UserType;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.users.repository.UserRepository;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.utils.file_upload.IFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService implements IKycService {
    private final IFileService fileService;
    private final IAuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${compliance.email}")
    String complianceEmail;
    @Override
    public Mono<Response<Boolean>> upload(MultipartFile[] fileList) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    //todo only allow registered businesses to upload kyc documents
                    log.info("about uploading kyc documents for user {}",user.getEmail());
                    return fileService.upload(user.getMerchantId(),fileList)
                            .flatMap(aBoolean -> {
                                notificationService.sendEmailAsync(EmailDTO.builder().to(complianceEmail).subject(user.getEmail()+" uploaded KYC documents for review").body("Documents uploaded for review").build()).subscribe();
                                return Mono.just(Response.<Boolean>builder()
                                                .data(aBoolean)
                                                .status(HttpStatus.OK)
                                                .statusCode(HttpStatus.OK.value())
                                                .message(SUCCESSFUL)
                                        .build());
                            }).doOnError(throwable -> {
                                log.error("error uploading files");
                                throw new CustomException(Response.builder()
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of("Error uploading files",throwable.getLocalizedMessage()))
                                        .message(FAILED)
                                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });

                });
    }

    @Override
    public Mono<Response<List<User>>> update(UpdateKycDTO request) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.ADMIN)||user.getUserType().equals(UserType.SUPER_ADMIN)){
                        log.info("about updating user with id {} by compliance officer {}",request.getUserId(),user.getEmail());

                        return userRepository.findByMerchantId(request.getUserId())
                                .collectList()
                                .flatMap(users -> {
                                    List<User>userList = new ArrayList<>();
                                    for (User user1:users) {
                                        user1.setKycCompleted(request.getStatus());
                                        userList.add(user1);
                                    }

                                    return userRepository.saveAll(userList)
                                            .collectList()
                                            .flatMap(users1 -> {
                                                log.info("kyc for merchant {} successfully updated",request.getUserId());
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

                                });
                    }
                    else{
                        log.error("user is not a compliant officer or super admin");
                        return Mono.just(Response.<List<User>>builder()
                                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .message(FAILED)
                                        .errors(List.of("user is not a compliance officer"))
                                .build());
                    }
                });
    }
}

package com.example.vestrapay.superadmin.compliance.service;

import com.example.vestrapay.merchant.keys.enums.KeyUsage;
import com.example.vestrapay.merchant.keys.interfaces.IKeyService;
import com.example.vestrapay.merchant.kyc.models.Document;
import com.example.vestrapay.merchant.notifications.interfaces.INotificationService;
import com.example.vestrapay.merchant.notifications.models.EmailDTO;
import com.example.vestrapay.merchant.users.enums.UserType;
import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.merchant.users.repository.UserRepository;
import com.example.vestrapay.superadmin.compliance.dtos.ValidateKYCDTO;
import com.example.vestrapay.superadmin.compliance.enums.ApprovalStatus;
import com.example.vestrapay.superadmin.compliance.interfaces.IComplianceService;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.utils.file_upload.IFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ComplianceService implements IComplianceService {
    private final IFileService fileService;

    private final INotificationService notificationService;
    private final UserRepository userRepository;
    private final IKeyService keyService;
    private final RestTemplate restTemplate;

    @Override
    public Mono<Response<?>> fetchAllDocumentsByMerchantId(String merchantId) {
        return fileService.loadAll(merchantId).flatMap(listResponse -> {
            listResponse.getData().forEach(document -> {

            });
            Response<Object> response = Response.builder()
                    .message("SUCCESS")
                    .statusCode(HttpStatus.OK.value())
                    .status(HttpStatus.OK)
                    .data(listResponse.getData())
                    .build();
            return Mono.just(response);

        });



    }



    @Override
    public Mono<Response<List<User>>> fetchAllPendingApprovals() {
        return userRepository.findByUserTypeAndKycCompleted(UserType.MERCHANT,false)
                .collectList()
                .flatMap(users -> Mono.just(Response.<List<User>>builder()
                        .message("SUCCESS")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .data(users)
                        .build()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("no pending merchant awaiting kyc approvals");
                    return Mono.just(Response.<List<User>>builder()
                                    .message("FAILED")
                                    .status(HttpStatus.NOT_FOUND)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .errors(List.of("no pending kyc approval"))
                                    .data(null)
                            .build());
                }));
    }

    @Override
    public Mono<Response<Object>> validateKYC(ValidateKYCDTO request) {
        return userRepository.findByMerchantIdAndUserType(request.getMerchantId(),UserType.MERCHANT)
                .flatMap(user -> {
                    return userRepository.findByMerchantId(request.getMerchantId())
                            .collectList()
                            .flatMap(users -> {
                                if (request.getStatus().equals(ApprovalStatus.APPROVED)){
                                    users.forEach(user1 -> user1.setKycCompleted(true));
                                }
                                else
                                    users.forEach(user1 -> user1.setKycCompleted(false));
                                return userRepository.saveAll(users)
                                        .collectList()
                                        .flatMap(users1 -> {
                                            if (request.getStatus().equals(ApprovalStatus.APPROVED)){
                                                CompletableFuture.runAsync(() -> keyService.adminGenerateProdKeys(KeyUsage.LIVE, user.getMerchantId()).subscribe());
                                            }
                                            CompletableFuture.runAsync(() -> notificationService.notifyAdmins("merchant "+user.getMerchantId()+" with email "+user.getEmail()
                                                                    +" KYC "+request.getStatus()+"\n Reason: "+request.getReason()
                                                            ,"KYC VALIDATION").subscribe());
                                            CompletableFuture.runAsync(() -> notificationService.sendEmailAsync(EmailDTO.builder()
                                                    .body("KYC Status is "+request.getStatus())
                                                    .subject("KYC VALIDATION STATUS")
                                                    .to(user.getEmail())
                                                    .build()).subscribe());
                                            return Mono.just(Response.builder()
                                                    .message("SUCCESSFUL")
                                                    .statusCode(HttpStatus.OK.value())
                                                    .status(HttpStatus.OK)
                                                    .data(request.getStatus())
                                                    .build());
                                        });
                            });

                }).switchIfEmpty(Mono.defer(() -> {
                    log.info("merchant not found with id {}",request.getMerchantId());
                    return Mono.just(Response.builder()
                            .message("FAILED")
                            .status(HttpStatus.NOT_FOUND)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .errors(List.of("merchant not found"))
                            .build());
                }));
    }


}

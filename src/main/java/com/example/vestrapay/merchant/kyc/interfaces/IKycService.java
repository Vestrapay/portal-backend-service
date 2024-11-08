package com.example.vestrapay.merchant.kyc.interfaces;

import com.example.vestrapay.merchant.kyc.dtos.UpdateKycDTO;
import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface IKycService {
    Mono<Response<Object>> upload(Map<String, MultipartFile[]> fileList);

    Mono<Response<List<User>>> update(UpdateKycDTO request);
}

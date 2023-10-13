package com.example.vestrapay.kyc.interfaces;

import com.example.vestrapay.kyc.dtos.UpdateKycDTO;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IKycService {
    Mono<Response<Boolean>> upload(MultipartFile[] fileList);

    Mono<Response<List<User>>> update(UpdateKycDTO request);
}

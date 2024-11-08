package com.example.vestrapay.utils.file_upload;

import com.example.vestrapay.merchant.kyc.models.Document;
import com.example.vestrapay.utils.dtos.Response;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface IFileService {

    Mono<Response<List<Document>>> loadAll(String merchantId);

    Mono<Boolean> upload(String merchantId, Map<String, MultipartFile[]> fileList);
    Mono<String> disputeUpload(String merchantId, MultipartFile fileList);
}

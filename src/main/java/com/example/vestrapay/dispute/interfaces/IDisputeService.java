package com.example.vestrapay.dispute.interfaces;

import com.example.vestrapay.dispute.dto.DisputeDTO;
import com.example.vestrapay.utils.dtos.Response;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IDisputeService {
    Mono<Response<Object>> logDispute(DisputeDTO request, MultipartFile[] files);

    Mono<Response<Object>> viewDisputeById(String uuid);

    Mono<Response<Object>> viewAllDispute();

    Mono<Response<Object>> viewByParam(Map<String, String> param);
}

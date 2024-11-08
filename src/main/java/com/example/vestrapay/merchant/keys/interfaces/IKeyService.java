package com.example.vestrapay.merchant.keys.interfaces;

import com.example.vestrapay.merchant.keys.enums.KeyUsage;
import com.example.vestrapay.merchant.keys.models.Keys;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

public interface IKeyService {
    Mono<Response<Keys>> generateKey(KeyUsage keyUsage);
    Mono<Response<Keys>> viewKeys(KeyUsage keyUsage);
    Mono<Response<Keys>> adminGenerateProdKeys(KeyUsage keyUsage,String merchantId);
}

package com.example.vestrapay.keys.interfaces;

import com.example.vestrapay.keys.enums.KeyUsage;
import com.example.vestrapay.keys.models.Keys;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

public interface IKeyService {
    Mono<Response<Keys>> generateKey(KeyUsage keyUsage);
    Mono<Response<Keys>> viewKeys(KeyUsage keyUsage);
}

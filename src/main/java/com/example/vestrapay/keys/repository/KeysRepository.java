package com.example.vestrapay.keys.repository;

import com.example.vestrapay.keys.models.Keys;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface KeysRepository extends R2dbcRepository<Keys,Long> {
    Mono<Keys>findByUserIdAndKeyUsage(String userId,String keyUsage);
}

package com.example.vestrapay.users.repository;

import com.example.vestrapay.users.models.User;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
public interface UserRepository extends R2dbcRepository<User,Long> {
    Mono<User> findUserByEmail(String email);
    Mono<User> findUserByEmailAndEnabled(String email,boolean enabled);
    @Modifying
    @Query("UPDATE app_user set enabled=true WHERE email = :email")
    Mono<Object> updateUserByEmail(@Param("email") String email);

    Flux<User>findByMerchantId(String merchantId);
    Mono<User>findByMerchantIdAndUuid(String merchantId,String uuid);

}

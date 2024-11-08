package com.example.vestrapay.merchant.notifications.repository;

import com.example.vestrapay.merchant.notifications.models.Notification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends R2dbcRepository<Notification,Long> {
    @Query("SELECT * FROM notifications ORDER BY id DESC LIMIT 10")
    Flux<Notification> findTopTen();
    @Query("SELECT * FROM notifications where merchant_id = :merchantId ORDER BY id DESC LIMIT 10")
    Flux<Notification> findTopTenByMerchantId(@Param("email") String merchantId);
    Flux<Notification> findAllByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}

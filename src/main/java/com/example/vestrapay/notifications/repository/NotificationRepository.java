package com.example.vestrapay.notifications.repository;

import com.example.vestrapay.notifications.models.Notification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NotificationRepository extends R2dbcRepository<Notification,Long> {
    @Query("SELECT * FROM notifications ORDER BY id DESC LIMIT 10")
    Flux<Notification> findTopTen();
}

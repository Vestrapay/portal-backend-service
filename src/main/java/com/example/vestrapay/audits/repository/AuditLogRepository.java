package com.example.vestrapay.audits.repository;

import com.example.vestrapay.audits.models.AuditEvent;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface AuditLogRepository extends R2dbcRepository<AuditEvent,Long> {
    @Modifying
    @Query("select * from audit_event where created_at between $1 and $2 order by id $3")
    Flux<AuditEvent> getLogs(LocalDateTime from, LocalDateTime to, String sortBy);
}

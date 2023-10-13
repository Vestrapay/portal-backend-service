package com.example.vestrapay.audits.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("audit_event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class AuditEvent {
    @Id
    @Column("id")
    private Long id;
    private String uuid;
    @Column("user_uuid")
    private String userUUID;
    private String event;
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

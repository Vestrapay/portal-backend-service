package com.example.vestrapay.dispute.entity;

import com.example.vestrapay.dispute.enums.DisputeEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("dispute_management")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Getter
@Setter
public class Dispute {
    @Id
    private Long id;
    private String uuid;
    @Column("merchant_id")
    private String merchantId;
    @Column("file_url")
    private String fileUrl;
    @Enumerated(EnumType.STRING)
    private DisputeEnum status;
    @Column("transaction_reference")
    private String transactionReference;
    private String comment;
    @CreatedDate
    @Column("created_at")
    private LocalDateTime dateCreated;
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime dateUpdated;


}

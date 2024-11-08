package com.example.vestrapay.merchant.kyc.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("document")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document {
    @Id
    @Column("id")
    private Long id;
    @Column("document_id")
    private String documentId;
    @Column("merchant_id")
    private String merchantId;
    @Column("document_name")
    private String documentName;
    @Column("file_url")
    private String file_url;
    @Column("date_created")
    private LocalDateTime dateCreated;
    @LastModifiedDate
    @Column("date_updated")
    private LocalDateTime dateUpdated;

}

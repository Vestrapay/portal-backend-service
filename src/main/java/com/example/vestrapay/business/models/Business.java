package com.example.vestrapay.business.models;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("business")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Business {
    @Id
    @Column("id")
    private Long id;
    private String uuid;
    @Column("merchant_id")
    private String merchantId;
    @Column("business_name")
    private String businessName;
    @Column("business_address")
    private String businessAddress;
    @Column("business_phone_number")
    private String businessPhoneNumber;
    @Column("business_email")
    private String businessEmail;
    @Column("business_support_phone_number")
    private String businessSupportPhoneNumber;
    @Column("business_support_email_address")
    private String businessSupportEmailAddress;
    private String country;
    @Column("chargeback_email")
    private String chargeBackEmail;
    @Column("customer_pay_txn_fee")
    private boolean customerPayTransactionFee;
    //notification

    @Column("notification_email")
    private boolean emailNotification;
    @Column("customer_email")
    private boolean customerNotification;
    @Column("credit_notification")
    private boolean creditNotifications;
    @Column("notify_business_email")
    private boolean notifyOnlyBusinessEmail;
    @Column("notify_dashboard_users")
    private boolean notifyDashboardUsers;
    @Column("send_to_specific_users")
    private String sendToSpecificUsers;


    //security
    @Column("two_factor_login")
    private boolean twoFAlogin;
    @Column("two_factor_for_transfer")
    private boolean twoFAForTransfer;
    @Column("transfer_via_api")
    private boolean transfersViaAPI;
    @Column("transfer_via_dashboard")
    private boolean transfersViaDashboard;
    @Column("disable_all_transfers")
    private boolean disableAllTransfers;
    //payment methods
    @Column("payment_methods")
    private String paymentMethod; //list of payment methods coma separated

    @Enumerated(EnumType.STRING)
    @Column("settlement_time")
    private String settlementTime;
    @CreatedDate
    @Column("date_created")
    private LocalDateTime dateCreated;
    @LastModifiedDate
    @Column("date_updated")
    private LocalDateTime dateUpdated;
}

package com.example.vestrapay.business.dtos;

import com.example.vestrapay.settlements.models.SettlementDurations;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BusinessDTO {
    private String businessName;
    private String businessAddress;
    private String businessPhoneNumber;
    private String businessEmail;
    private String businessSupportPhoneNumber;
    private String businessSupportEmailAddress;
    private String country;
    private String chargeBackEmail;
    private boolean customerPayTransactionFee;

    //notification
    private boolean emailNotification;
    private boolean customerNotification;
    private boolean creditNotifications;
    private boolean notifyOnlyBusinessEmail;
    private boolean notifyDashboardUsers;
    private String sendToSpecificUsers;

    //security
    private boolean twoFAlogin;
    private boolean twoFAForTransfer;
    private boolean transfersViaAPI;
    private boolean transfersViaDashboard;
    private boolean disableAllTransfers;

    //payment methods
    private String paymentMethod; //list of payment methods coma separated
    private String settlementTime;

}

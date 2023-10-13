package com.example.vestrapay.dashboard;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticsDTO {
    private String recentTransactions;
    private String systemUsers;
    private String loggedIssues;
    private String recentNotifications;
}

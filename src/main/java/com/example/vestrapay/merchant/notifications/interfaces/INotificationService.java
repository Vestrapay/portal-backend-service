package com.example.vestrapay.merchant.notifications.interfaces;

import com.example.vestrapay.merchant.notifications.models.EmailDTO;
import reactor.core.publisher.Mono;

public interface INotificationService {
    Mono<Void> sendEmailAsync(EmailDTO email);
    Mono<Void> notifyAdmins(String body,String subject);
}

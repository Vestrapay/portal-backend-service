package com.example.vestrapay.notifications.interfaces;

import com.example.vestrapay.notifications.models.EmailDTO;
import reactor.core.publisher.Mono;

public interface INotificationService {
    Mono<Void> sendEmailAsync(EmailDTO email);
    Mono<Void> notifyAdmins(String body);
}

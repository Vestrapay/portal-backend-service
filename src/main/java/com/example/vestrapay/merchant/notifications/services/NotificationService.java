package com.example.vestrapay.merchant.notifications.services;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.merchant.authentications.services.AuthenticationService;
import com.example.vestrapay.merchant.users.repository.UserRepository;
import com.example.vestrapay.merchant.notifications.interfaces.INotificationService;
import com.example.vestrapay.merchant.notifications.models.EmailDTO;
import com.example.vestrapay.merchant.notifications.models.Notification;
import com.example.vestrapay.merchant.notifications.repository.NotificationRepository;
import com.example.vestrapay.merchant.users.enums.UserType;
import com.example.vestrapay.utils.dtos.Response;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Properties;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {
    @Value("${spring.mail.username}")
    String senderEmail;
    @Value("${spring.mail.password}")
    String senderPassword;
    @Value("${admin.notification.email}")
    String defaultNotificationEmail;
    private final UserRepository userRepository;
    @Override
    public Mono<Void> sendEmailAsync(EmailDTO email) {
        Mono<Boolean> processMono = Mono.fromCallable(() -> {
            send(email);
            return true;
        });
        processMono.subscribeOn(Schedulers.boundedElastic()).subscribe();
        return Mono.empty();
    }

    @Override
    public Mono<Void> notifyAdmins(String body,String subject) {
        return userRepository.findByUserType(UserType.ADMIN)
                .collectList()
                .flatMap(users -> {
                    users.forEach(user -> {
                        send(EmailDTO.builder()
                                .to(user.getEmail())
                                .subject(subject)
                                .body(body)
                                .cc(new String[]{defaultNotificationEmail})
                                .build());
                    });
                    return Mono.empty();
                });
    }

    public void send(EmailDTO request){

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Create a MimeMessage
            Message message = new MimeMessage(session);

            // Set the sender and recipient addresses
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(request.getTo()));

            // Set the subject and content of the email
            message.setSubject(request.getSubject());
            message.setText(request.getBody());

            // Send the email
            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }



}

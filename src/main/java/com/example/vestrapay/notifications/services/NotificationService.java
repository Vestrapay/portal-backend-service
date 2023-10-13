package com.example.vestrapay.notifications.services;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.notifications.interfaces.INotificationService;
import com.example.vestrapay.notifications.models.EmailDTO;
import com.example.vestrapay.notifications.models.Notification;
import com.example.vestrapay.notifications.repository.NotificationRepository;
import com.example.vestrapay.utils.dtos.Response;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    @Value("${notification.email}")
    String senderEmail;
    @Value("${notification.password}")
    String senderPassword;
    private final NotificationRepository notificationRepository;
    @Override
    public Mono<Void> sendEmailAsync(EmailDTO email) {
        Mono<Boolean> processMono = Mono.fromCallable(() -> {
            send(email);
            return true;
        });
        processMono.subscribeOn(Schedulers.boundedElastic()).subscribe();
        return Mono.empty();
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


    public Mono<Response<List<Notification>>> getTop10Notification(){
        log.info("about fetching top 10 notification");
        return notificationRepository.findTopTen().collectList()
                .flatMap(notifications -> {
                    log.info("top to notifications gotten");
                    return Mono.just(Response.<List<Notification>>builder()
                                    .data(notifications)
                                    .message(SUCCESSFUL)
                                    .statusCode(HttpStatus.OK.value())
                                    .status(HttpStatus.OK)
                            .build());

                }).doOnError(throwable -> {
                    log.info("error fetching top 10 notifications");
                    throw new CustomException(Response.<Void>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error creating account for user",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

}

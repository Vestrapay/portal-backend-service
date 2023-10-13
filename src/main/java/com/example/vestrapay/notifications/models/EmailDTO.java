package com.example.vestrapay.notifications.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class EmailDTO {
    private String to;
    private String subject;
    private String[] cc;
    private Object attachment;
    private String body;
    private String template;
}

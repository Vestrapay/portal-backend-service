package com.example.vestrapay.utils.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedRequest {
    private LocalDateTime from;
    private LocalDateTime to;
    private String sortBy;
    private String prop;
}

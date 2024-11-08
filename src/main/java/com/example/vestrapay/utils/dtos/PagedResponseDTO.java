package com.example.vestrapay.utils.dtos;

import lombok.*;

import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedResponseDTO {
    private int pageNumber;

    private int pageSize;

    private int total;

    private Long totalPages;

    private boolean status;

    private int statusCode;

    private String message;

    private Object data;

    private Set<Error> errors;
}

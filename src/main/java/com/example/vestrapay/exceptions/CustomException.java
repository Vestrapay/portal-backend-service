package com.example.vestrapay.exceptions;

import com.example.vestrapay.utils.dtos.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomException extends RuntimeException {

    private Response response;
    private HttpStatus httpStatus;

    public CustomException customException(Response response, HttpStatus httpStatus) {
        Response error =  Response.builder()
                .statusCode(httpStatus.value())
                .message(httpStatus.getReasonPhrase())
                .build();
        return CustomException.builder()
                .response(error)
                .httpStatus(httpStatus)
                .build();
    }

    public CustomException(Throwable cause) {
        super(cause);
    }
}

package com.example.vestrapay.exceptions;

import com.example.vestrapay.utils.dtos.Response;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public final ResponseEntity<Response> handleError(CustomException ex) {

        logError(ex);
        return new ResponseEntity<>(ex.getResponse(), ex.getHttpStatus());
    }
    @ExceptionHandler(ServletException.class)
    public final ResponseEntity<Response> handleError(ServletException ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public final ResponseEntity<Response> handleError(ExpiredJwtException ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }



    @ExceptionHandler(TimeoutException.class)
    public final ResponseEntity<Response> handleError(TimeoutException ex) {
        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.REQUEST_TIMEOUT), HttpStatus.REQUEST_TIMEOUT);
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public final ResponseEntity<Response> handleError(UsernameNotFoundException ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(IOException.class)
    public final ResponseEntity<Response> handleError(IOException ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<Response> handleError(IllegalArgumentException ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public final ResponseEntity<Response> handleError(MissingServletRequestParameterException ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public final ResponseEntity<Response> handleError(MissingRequestHeaderException ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.BAD_REQUEST), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ArithmeticException.class)
    public final ResponseEntity<Response> handleError(ArithmeticException ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public final ResponseEntity<Response> handleError(HttpMessageNotReadableException ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Response> handleError(Exception ex) {

        logError(ex);
        return new ResponseEntity<>(getResponse(ex,HttpStatus.SERVICE_UNAVAILABLE), HttpStatus.SERVICE_UNAVAILABLE);
    }

    private void logError(Exception ex) {
        log.info("Exception thrown :: {}, Message :: {}", ex.getClass().getSimpleName(), ex.getLocalizedMessage());
        ex.printStackTrace();
    }

    private Response getResponse(Exception ex, HttpStatus httpStatus) {

        return Response.builder()
                .message(httpStatus.getReasonPhrase())
                .statusCode(httpStatus.value())
                .errors(List.of(ex.getMessage()))
                .build();
    }


}

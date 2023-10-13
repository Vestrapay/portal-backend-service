package com.example.vestrapay.paymentmethods.services;

import com.example.vestrapay.paymentmethods.interfaces.IPaymentMethodInterface;
import com.example.vestrapay.utils.dtos.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@Slf4j
public class PaymentMethodService implements IPaymentMethodInterface {
    @Value("${payment.methods}")
    private List<String>paymentMethods;
    @Override
    public Mono<Response<?>> getAllMethods() {
        return Mono.just(Response.builder()
                        .data(paymentMethods)
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .message(SUCCESSFUL)
                .build());
    }
}

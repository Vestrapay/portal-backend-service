package com.example.vestrapay.payment.services;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.payment.dtos.PaymentProvidersDTO;
import com.example.vestrapay.payment.dtos.RouteRuleDTO;
import com.example.vestrapay.payment.interfaces.IPaymentMethodInterface;
import com.example.vestrapay.payment.model.PaymentMethods;
import com.example.vestrapay.payment.model.PaymentProviders;
import com.example.vestrapay.payment.model.RoutingRule;
import com.example.vestrapay.payment.repository.PaymentMethodRepository;
import com.example.vestrapay.payment.repository.PaymentProviderRepository;
import com.example.vestrapay.payment.repository.RoutingRuleRepository;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService implements IPaymentMethodInterface {
    public static final String FAILED = "Failed";
    public static final String SUCCESSFUL = "Successful";
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final RoutingRuleRepository routingRuleRepository;

    @Override
    public Mono<Response<?>> getAllMethods() {
        return paymentMethodRepository.findAll()
                .collectList()
                .flatMap(paymentMethods -> Mono.just(Response.builder()
                        .data(paymentMethods)
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .message(SUCCESSFUL)
                        .build()));

    }

    @Override
    public Mono<Response<Object>> createPaymentMethod(String name) {
        return paymentMethodRepository.findByName(name)
                .flatMap(paymentMethods -> {
                    log.error("method already exist with name {}",name);
                    return Mono.just(Response.builder()
                                    .message(FAILED)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .errors(List.of("method already exist"))
                                    .data(paymentMethods)
                                    .status(HttpStatus.CONFLICT)
                            .build());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    return paymentMethodRepository.save(PaymentMethods.builder().build())
                            .flatMap(paymentMethods -> {
                                return Mono.just(Response.builder()
                                                .message(SUCCESSFUL)
                                                .status(HttpStatus.CREATED)
                                                .statusCode(HttpStatus.CREATED.value())
                                                .data(paymentMethods)
                                        .build());

                            });
                }))
                .doOnError(throwable -> {
                    log.error("error creating payment method. error is {}",throwable.getMessage());
                    throw new CustomException(throwable);
                });
    }

    @Override
    public Mono<Response<Object>> registerProvider(PaymentProvidersDTO request) {

        return paymentProviderRepository.findByName(request.getName())
                .flatMap(paymentProviders -> {
                    log.error("payment provider {} already exist",request.getName());
                    return Mono.just(Response.builder()
                                    .errors(List.of("Payment provider already exist with name "+request.getName()))
                                    .status(HttpStatus.CONFLICT)
                                    .message(FAILED)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .data(paymentProviders)
                            .build());
                }).switchIfEmpty(Mono.defer(() -> {
                    return paymentProviderRepository.save(PaymentProviders.builder()
                                    .uuid(UUID.randomUUID().toString())
                                    .supportedPaymentMethods(request.getSupportedPaymentMethods())
                                    .name(request.getName())
                            .build()).flatMap(paymentProviders -> {
                        return Mono.just(Response.builder()
                                .status(HttpStatus.CREATED)
                                .message(SUCCESSFUL)
                                .statusCode(HttpStatus.CREATED.value())
                                .data(paymentProviders)
                                .build());
                    });

                })).doOnError(throwable -> {
                    log.error("error registering provider, error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(throwable.getLocalizedMessage())
                            .errors(List.of(throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Object>> updateProvider(PaymentProvidersDTO request) {
        return paymentProviderRepository.findByName(request.getName())
                .flatMap(paymentProviders -> {
                    paymentProviders.setName(request.getName());
                    paymentProviders.setSupportedPaymentMethods(request.getSupportedPaymentMethods());
                    return paymentProviderRepository.save(paymentProviders)
                            .flatMap(result -> {
                                return Mono.just(Response.builder()
                                        .status(HttpStatus.OK)
                                        .message(SUCCESSFUL)
                                        .statusCode(HttpStatus.OK.value())
                                        .data(result)
                                        .build());
                    });



                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("payment provider {} does not already exist",request.getName());
                    return Mono.just(Response.builder()
                            .errors(List.of("Payment provider does not exist with name "+request.getName()))
                            .status(HttpStatus.CONFLICT)
                            .message(FAILED)
                            .statusCode(HttpStatus.CONFLICT.value())
                            .build());
                })).doOnError(throwable -> {
                    log.error("error updating provider, error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(throwable.getLocalizedMessage())
                            .errors(List.of(throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Object>> viewAllProviders() {
        return paymentProviderRepository.findAll()
                .collectList()
                .flatMap(paymentProviders -> {
                    return Mono.just(Response.builder()
                                    .data(paymentProviders)
                                    .message(SUCCESSFUL)
                                    .statusCode(HttpStatus.OK.value())
                                    .status(HttpStatus.OK)
                            .build());
                }).doOnError(throwable -> {
                    log.error("error viewing all providers, error is {}",throwable.getMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(throwable.getLocalizedMessage())
                            .errors(List.of(throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Object>> viewAllProvidersByPaymentMethod(String paymentMethod) {
        return paymentProviderRepository.findAll()
                .collectList()
                .flatMap(paymentProviders -> {
                    Set<PaymentProviders> collect = paymentProviders.stream().filter(paymentProviders1 -> paymentProviders1.getSupportedPaymentMethods().contains(paymentMethod)).collect(Collectors.toSet());
                    return Mono.just(Response.builder()
                            .data(collect)
                            .message(SUCCESSFUL)
                            .statusCode(HttpStatus.OK.value())
                            .status(HttpStatus.OK)
                            .build());
                }).switchIfEmpty(Mono.defer(() -> {
                    return Mono.just(Response.builder()
                            .errors(List.of("Provider not found with method "+paymentMethod))
                            .message(FAILED)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
                }));
    }

    @Override
    public Mono<Response<Object>> viewProviderByUuid(String uuid) {
        return paymentProviderRepository.findByUuid(uuid)
                .flatMap(paymentProviders -> {
                    return Mono.just(Response.builder()
                            .data(paymentProviders)
                            .message(SUCCESSFUL)
                            .statusCode(HttpStatus.OK.value())
                            .status(HttpStatus.OK)
                            .build());
                }).switchIfEmpty(Mono.defer(() -> {
                    return Mono.just(Response.builder()
                            .errors(List.of("Provider not found with UUID "+uuid))
                            .message(FAILED)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
                }));
    }

    @Override
    public Mono<Response<Object>> configureRoute(RouteRuleDTO request) {
        return routingRuleRepository.findByPaymentMethod(request.getPaymentMethod())
                .flatMap(routingRule -> {
                    return Mono.just(Response.builder()
                                    .message(FAILED)
                                    .status(HttpStatus.CONFLICT)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .errors(List.of("routing rule already exist for method "+request.getPaymentMethod()))
                            .build());
                }).switchIfEmpty(Mono.defer(() -> {
                    return paymentMethodRepository.findAll().collectList().flatMap(paymentMethods -> {
                        List<String> collect = paymentMethods.stream().map(PaymentMethods::getName).toList();
                        if (collect.contains(request.getPaymentMethod())){
                            return paymentProviderRepository.findByName(request.getProvider())
                                    .flatMap(paymentProviders -> {
                                        Set<String> methods = new HashSet<>();
                                        paymentProviders.getSupportedPaymentMethods().stream().forEach(s -> {
                                            String value = s.replace("}","").replace("{","");
                                            methods.add(value);
                                        });
                                        if (methods.contains(request.getPaymentMethod())){
                                            return routingRuleRepository.save(RoutingRule.builder()
                                                            .uuid(UUID.randomUUID().toString())
                                                            .paymentMethod(request.getPaymentMethod())
                                                            .provider(request.getProvider())
                                                            .build())
                                                    .flatMap(routingRule -> {
                                                        return Mono.just(Response.builder()
                                                                .message(SUCCESSFUL)
                                                                .status(HttpStatus.CREATED)
                                                                .statusCode(HttpStatus.CREATED.value())
                                                                .data(routingRule)
                                                                .build());
                                                    });
                                        }
                                        return Mono.just(Response.builder()
                                                .message(FAILED)
                                                .status(HttpStatus.BAD_REQUEST)
                                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                                .errors(List.of("payment provider does not support method"))
                                                .build());

                                    })
                                    .switchIfEmpty(Mono.defer(() -> {
                                        return Mono.just(Response.builder()
                                                .message(FAILED)
                                                .status(HttpStatus.BAD_REQUEST)
                                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                                .errors(List.of("payment provider does not exist"))
                                                .build());
                                    }));

                        }
                        return Mono.just(Response.builder()
                                .message(FAILED)
                                .status(HttpStatus.BAD_REQUEST)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                        .errors(List.of("payment method not supported for "+request.getPaymentMethod()))
                                .build());
                    });


                })).doOnError(throwable -> {
                    log.error("error creating routing rule. error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(throwable.getLocalizedMessage())
                            .errors(List.of(throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Object>> updateRoute(RouteRuleDTO request) {
        return routingRuleRepository.findByPaymentMethod(request.getPaymentMethod())
                .flatMap(routingRule -> {
                    return paymentMethodRepository.findAll().collectList().flatMap(paymentMethods -> {
                        List<String> collect = paymentMethods.stream().map(PaymentMethods::getName).toList();
                        if (collect.contains(request.getPaymentMethod())){
                            return paymentProviderRepository.findByName(request.getProvider())
                                    .flatMap(paymentProviders -> {
                                        Set<String> methods = new HashSet<>();
                                        paymentProviders.getSupportedPaymentMethods().stream().forEach(s -> {
                                            String value = s.replace("}","").replace("{","");
                                            methods.add(value);
                                        });
                                        if (methods.contains(request.getPaymentMethod())){
                                            routingRule.setProvider(request.getProvider());
                                            return routingRuleRepository.save(routingRule)
                                                    .flatMap(rule -> {
                                                        return Mono.just(Response.builder()
                                                                .message(SUCCESSFUL)
                                                                .status(HttpStatus.CREATED)
                                                                .statusCode(HttpStatus.CREATED.value())
                                                                .data(rule)
                                                                .build());
                                                    });
                                        }
                                        return Mono.just(Response.builder()
                                                .message(FAILED)
                                                .status(HttpStatus.BAD_REQUEST)
                                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                                .errors(List.of("payment provider does not support method"))
                                                .build());

                                    })
                                    .switchIfEmpty(Mono.defer(() -> {
                                        return Mono.just(Response.builder()
                                                .message(FAILED)
                                                .status(HttpStatus.BAD_REQUEST)
                                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                                .errors(List.of("payment provider does not exist"))
                                                .build());
                                    }));

                        }
                        return Mono.just(Response.builder()
                                .message(FAILED)
                                .status(HttpStatus.BAD_REQUEST)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .errors(List.of("payment method not supported for "+request.getPaymentMethod()))
                                .build());
                    });
                }).switchIfEmpty(Mono.defer(() -> {
                    return Mono.just(Response.builder()
                            .message(FAILED)
                            .status(HttpStatus.BAD_REQUEST)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .errors(List.of("routing logic not found for "+request.getPaymentMethod()))
                            .build());


                })).doOnError(throwable -> {
                    log.error("error creating routing rule. error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(throwable.getLocalizedMessage())
                            .errors(List.of(throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });


    }


}

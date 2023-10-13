package com.example.vestrapay.users.services;

import com.example.vestrapay.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.notifications.models.EmailDTO;
import com.example.vestrapay.notifications.services.NotificationService;
import com.example.vestrapay.roles_and_permissions.interfaces.IRoleService;
import com.example.vestrapay.roles_and_permissions.repository.RolePermissionRepository;
import com.example.vestrapay.settlements.interfaces.ISettlementService;
import com.example.vestrapay.users.dtos.MerchantUserDTO;
import com.example.vestrapay.users.dtos.UserDTO;
import com.example.vestrapay.users.enums.UserType;
import com.example.vestrapay.users.interfaces.IUserService;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.users.repository.UserRepository;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.utils.service.OtpUtils;
import com.example.vestrapay.utils.service.RedisUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final RedisUtility redisUtility;
    private final OtpUtils otpUtils;
    private final IAuthenticationService authenticationService;
    private final IRoleService roleService;
    private final RolePermissionRepository rolePermissionRepository;
    private final ISettlementService settlementService;
    @Override
    public Mono<Response<Void>> createAccount(UserDTO request) {
        return userRepository.findUserByEmail(request.getEmail())
                .flatMap(user -> {
                    log.error("user already exists with email {}",request.getEmail());
                    return Mono.just(Response.<Void>builder()
                                    .status(HttpStatus.CONFLICT)
                                    .errors(List.of("email already exists for ".concat(request.getEmail())))
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .message(FAILED)
                            .build());
                }).switchIfEmpty(Mono.defer(() -> {
                    var otp = otpUtils.generateOTP(6);
                    User user = modelMapper.map(request, User.class);
                    user.setUuid(UUID.randomUUID().toString());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setEnabled(false);
                    user.setOtp(otp);
                    user.setUserType(UserType.MERCHANT);
                    user.setMerchantId(user.getUuid());
                    user.setKycCompleted(false);
                    return userRepository.save(user)
                            .flatMap(user1 -> {
                                redisUtility.setValue(user1.getEmail()+"_OTP",otp,300);
                                log.info("OTP for user {} is {}",user1.getEmail(),otp);
                                notificationService.sendEmailAsync(EmailDTO.builder()
                                        .to(user.getEmail())
                                        .subject("OTP VERIFICATION")
                                        .body(otp)
                                        .build()).subscribe();
                                settlementService.generateWemaAccountForMerchant(user1).subscribe();
                                log.info("user successfully registered");
                                roleService.createDefaultRole(user.getUuid(), user.getMerchantId(), "MERCHANT").subscribe();
                                return Mono.just(Response.<Void>builder()
                                                .statusCode(HttpStatus.CREATED.value())
                                                .status(HttpStatus.CREATED)
                                                .message(SUCCESSFUL)
                                        .build());
                            }).doOnError(throwable -> {
                                log.error("error registering user. error is {}",throwable.getLocalizedMessage());
                                throw new CustomException(Response.<Void>builder()
                                        .message(FAILED)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of(throwable.getLocalizedMessage(),throwable.getMessage(),throwable.getCause().toString()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });

                })).doOnError(throwable -> {
                    log.error("error creating account for user {}", throwable.getLocalizedMessage());
                    throw new CustomException(Response.<Void>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error creating account for user",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Void>> createMerchantUsers(String merchantId, MerchantUserDTO request) {
        log.info("about creating Merchant User with DTO {}",request.toString());
        return authenticationService.getLoggedInUser().flatMap(user -> {
            if (user.getUserType().equals(UserType.MERCHANT)){
                return userRepository.findUserByEmail(request.getEmail())
                        .flatMap(user1 -> {
                            log.error("merchant user already exists with email {}",request.getEmail());
                            return Mono.just(Response.<Void>builder()
                                    .status(HttpStatus.CONFLICT)
                                    .errors(List.of("email already exists for ".concat(request.getEmail())))
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .message(FAILED)
                                    .build());
                        }).switchIfEmpty(Mono.defer(() -> {
                            var otp = otpUtils.generateOTP(6);
                            User merchantUser = modelMapper.map(request, User.class);
                            merchantUser.setUuid(UUID.randomUUID().toString());
                            merchantUser.setPassword(passwordEncoder.encode(request.getPassword()));
                            merchantUser.setEnabled(false);
                            merchantUser.setOtp(otp);
                            merchantUser.setUserType(UserType.MERCHANT_USER);
                            merchantUser.setMerchantId(user.getMerchantId());
                            merchantUser.setBusinessName(user.getBusinessName());
                            return userRepository.save(merchantUser)
                                    .flatMap(user1 -> {
                                        redisUtility.setValue(request.getEmail()+"_OTP",otp,300);
                                        log.info("OTP for user {} is {}",user.getEmail(),otp);
                                        notificationService.sendEmailAsync(EmailDTO.builder()
                                                .to(user.getEmail())
                                                .subject("OTP VERIFICATION")
                                                .body("OTP: "+otp.concat("Password is ").concat(request.getPassword()))
                                                .build()).subscribe();
                                        log.info("user successfully registered");
                                        roleService.createDefaultRole(user.getUuid(),user.getMerchantId(),"MERCHANT_USER").subscribe();
                                        return Mono.just(Response.<Void>builder()
                                                .statusCode(HttpStatus.CREATED.value())
                                                .status(HttpStatus.CREATED)
                                                .message(SUCCESSFUL)
                                                .build());
                                    }).doOnError(throwable -> {
                                        log.error("error registering user. error is {}",throwable.getLocalizedMessage());
                                        throw new CustomException(Response.<Void>builder()
                                                .message(FAILED)
                                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .errors(List.of(throwable.getLocalizedMessage(),throwable.getMessage(),throwable.getCause().toString()))
                                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                    });

                        })).doOnError(throwable -> {
                            log.error("error creating account for user {}", throwable.getLocalizedMessage());
                            throw new CustomException(Response.<Void>builder()
                                    .message(FAILED)
                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .errors(List.of("error creating account for user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                        });
            }else {
                log.error("logged in user is not a merchant {}",user.getEmail());
                return Mono.just(Response.<Void>builder()
                                .status(HttpStatus.UNAUTHORIZED)
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .message(FAILED)
                                .errors(List.of("logged in user is not a merchant"))
                        .build());
            }

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("user not logged in or exist");
            return Mono.just(Response.<Void>builder()
                            .status(HttpStatus.NOT_FOUND)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(FAILED)
                            .errors(List.of("user not logged in or found"))
                    .build());
        })).doOnError(throwable -> {
            log.error("error fetching logged in user, error is {}", throwable.getLocalizedMessage());
            throw new CustomException(Response.<Void>builder()
                    .message(FAILED)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage(),throwable.getMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    @Override
    public Mono<Response<User>> updateMerchantUsers(String merchantId, User request) {
        log.info("about updating merchant user with DTO {}",request.toString());

        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.MERCHANT)){
                        return userRepository.findUserByEmail(request.getEmail())
                                .flatMap(user1 -> {
                                    user1.setCountry(request.getCountry());
                                    user1.setFirstName(request.getFirstName());
                                    user1.setLastName(request.getLastName());
                                    user1.setEmail(request.getEmail());
                                    user1.setPhoneNumber(request.getPhoneNumber());
                                    user1.setEnabled(request.isEnabled());

                                    return userRepository.save(user1).flatMap(user2 -> {
                                        log.info("user updated successfully");
                                        return Mono.just(Response.<User>builder()
                                                .data(user2)
                                                .status(HttpStatus.OK)
                                                .statusCode(HttpStatus.OK.value())
                                                .message(SUCCESSFUL)
                                                .build());
                                    }).doOnError(throwable -> {
                                        log.error("error updating user. error is {}",throwable.getLocalizedMessage());
                                        throw new CustomException(Response.<User>builder()
                                                .message(FAILED)
                                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .errors(List.of("error saving updated user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                    });
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.error("user not logged in or exist");
                                    return Mono.just(Response.<User>builder()
                                            .status(HttpStatus.NOT_FOUND)
                                            .statusCode(HttpStatus.NOT_FOUND.value())
                                            .message(FAILED)
                                            .errors(List.of("user not logged in or found"))
                                            .build());
                                }));

                    }
                    else {
                        log.error("user is not a merchant or merchant user");
                        return Mono.just(Response.<User>builder()
                                .status(HttpStatus.UNAUTHORIZED)
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .message(FAILED)
                                .errors(List.of("permission denied"))
                                .build());
                    }

                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<User>builder()
                            .status(HttpStatus.NOT_FOUND)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(FAILED)
                            .errors(List.of("user not logged in or found"))
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error("error fetching logged in user, error is {}", throwable.getLocalizedMessage());
                    throw new CustomException(Response.<Void>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }



    @Override
    public Mono<Response<Void>> deleteMerchantUsers(String merchantId, String userId) {
        log.info("about deleting merchant user with Id {}",userId);
        String finalUserID = userId.replace("\"","");
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.MERCHANT)){
                        log.info("logged in user {} about to delete merchant user {}",user.getUuid(),finalUserID);
                        return userRepository.findByMerchantIdAndUuid(user.getMerchantId(),finalUserID)
                                .flatMap(user1 -> {
                                    log.info("user found");
                                    if (user.getUuid().equals(finalUserID)){
                                        log.error("user is logged in merchant");
                                        return Mono.just(Response.<Void>builder()
                                                .message(FAILED)
                                                .status(HttpStatus.NOT_FOUND)
                                                .statusCode(HttpStatus.NOT_FOUND.value())
                                                .errors(List.of("user is logged in merchant"))
                                                .build());
                                    }

                                    userRepository.delete(user1).subscribe();
                                    rolePermissionRepository.deleteAllByUserId(user1.getUuid()).subscribe();
                                    return Mono.just(Response.<Void>builder()
                                            .message(SUCCESSFUL)
                                            .statusCode(HttpStatus.OK.value())
                                            .status(HttpStatus.OK)
                                            .build());


                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.error("user not found");
                                    return Mono.just(Response.<Void>builder()
                                            .message(FAILED)
                                            .status(HttpStatus.NOT_FOUND)
                                            .statusCode(HttpStatus.NOT_FOUND.value())
                                            .errors(List.of("merchant user not found"))
                                            .build());
                                }))
                                .doOnError(throwable -> {
                                    log.error("error fetching merchant user {}",throwable.getLocalizedMessage());
                                    throw new CustomException(Response.<User>builder()
                                            .message(FAILED)
                                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .errors(List.of("error fetching merchant user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                });
                    }
                    else {
                        log.error("user is not a merchant");
                        return Mono.just(Response.<Void>builder()
                                        .message(FAILED)
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                                        .errors(List.of("User is not a merchant"))
                                .build());
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not found or registered. user is");
                    return Mono.just(Response.<Void>builder()
                                    .errors(List.of("user not found or logged in"))
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                                    .message(FAILED)
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error("Error fetching logged in user. error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.<User>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<List<User>>> merchantViewAllUsers() {
        log.info("about viewing all users for merchant");

        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.MERCHANT)){
                        return userRepository.findByMerchantId(user.getMerchantId())
                                .collectList()
                                .flatMap(users -> {
                                    log.info("merchant users for merchant {} is {}",user.getMerchantId(),users);
                                    return Mono.just(Response.<List<User>>builder()
                                                    .message(SUCCESSFUL)
                                                    .status(HttpStatus.OK)
                                                    .statusCode(HttpStatus.OK.value())
                                                    .data(users)
                                            .build());

                                }).doOnError(throwable -> {
                                    log.error("error fetching merchant users from repository error is {}",throwable.getLocalizedMessage());
                                    throw new CustomException(Response.<List<User>>builder()
                                            .message(FAILED)
                                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .errors(List.of("error fetching merchant users from repository",throwable.getLocalizedMessage(),throwable.getMessage()))
                                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                });
                    }
                    else {
                        log.error("user is not a merchant and cannot view merchant users");
                        return Mono.just(Response.<List<User>>builder()
                                        .errors(List.of("User is not a merchant"))
                                        .message(FAILED)
                                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                                        .status(HttpStatus.UNAUTHORIZED)
                                .build());
                    }

                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<List<User>>builder()
                            .status(HttpStatus.NOT_FOUND)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(FAILED)
                            .errors(List.of("user not logged in or found"))
                            .build());
                })).doOnError(throwable -> {
                    log.error("error fetching logged in user, error is {}", throwable.getLocalizedMessage());
                    throw new CustomException(Response.<Void>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }


    @Override
    public Mono<Response<User>> updateUser(UserDTO request) {
        log.info("about updating user with DTO {}",request.toString());
        return authenticationService.getLoggedInUser().flatMap(user -> {
            user.setBusinessName(request.getBusinessName());
            user.setCountry(request.getCountry());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPhoneNumber(request.getPhoneNumber());
            return userRepository.save(user)
                    .flatMap(user1 -> {
                        log.info("user successfully updated");
                        return Mono.just(Response.<User>builder()
                                .data(user1)
                                .message(SUCCESSFUL)
                                .status(HttpStatus.OK)
                                .statusCode(HttpStatus.OK.value())
                                .build());
                    }).doOnError(throwable -> {
                        log.error("error saving updated user, error is {}", throwable.getLocalizedMessage());
                        throw new CustomException(Response.<User>builder()
                                .message(FAILED)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .errors(List.of("error saving updated user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                    });
        }).switchIfEmpty(Mono.defer(() -> {
            log.error("user not logged in");
            return Mono.just(Response.<User>builder()
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .status(HttpStatus.UNAUTHORIZED)
                            .message(FAILED)
                            .errors(List.of("User not logged in"))
                    .build());
        })).doOnError(throwable -> {
            log.error("error updating user {}", throwable.getLocalizedMessage());
            throw new CustomException(Response.<Void>builder()
                    .message(FAILED)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errors(List.of("error updating user",throwable.getLocalizedMessage(),throwable.getMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    @Override
    public Mono<Response<User>> viewUser() {
        return authenticationService.getLoggedInUser().flatMap(user -> {
            log.info("user gotten for email {}",user.getEmail());
            return Mono.just(Response.<User>builder()
                            .message(SUCCESSFUL)
                            .data(user)
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                    .build());
        }).switchIfEmpty(Mono.defer(() -> {
            log.error("User not found or logged in");
            return Mono.just(Response.<User>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .message(FAILED)
                            .errors(List.of("User not logged in or registered"))
                    .build());
        })).doOnError(throwable -> {
            log.error("error viewing user {}", throwable.getLocalizedMessage());
            throw new CustomException(Response.<User>builder()
                    .message(FAILED)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errors(List.of("error viewing user",throwable.getLocalizedMessage(),throwable.getMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    @Override
    public Mono<Response<Void>> deleteUser(String userId) {
        return Mono.empty();
    }


}

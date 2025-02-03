package com.example.vestrapay.merchant.authentications.services;

import com.example.vestrapay.merchant.authentications.dtos.*;
import com.example.vestrapay.merchant.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.configs.JwtService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.merchant.business.repository.BusinessRepository;
import com.example.vestrapay.merchant.keys.enums.KeyUsage;
import com.example.vestrapay.merchant.keys.models.Keys;
import com.example.vestrapay.merchant.keys.repository.KeysRepository;
import com.example.vestrapay.merchant.users.enums.UserType;
import com.example.vestrapay.merchant.users.repository.UserRepository;
import com.example.vestrapay.merchant.notifications.interfaces.INotificationService;
import com.example.vestrapay.merchant.notifications.models.EmailDTO;
import com.example.vestrapay.merchant.roles_and_permissions.services.DefaultRoleService;
import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.utils.service.KeyUtility;
import com.example.vestrapay.utils.service.OtpUtils;
import com.example.vestrapay.utils.service.RedisUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.example.vestrapay.utils.PasswordUtil.isValidPassword;
import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService implements IAuthenticationService {
    private final UserRepository userRepository;
    private final RedisUtility redisUtility;
    private final OtpUtils otpUtils;
    private final INotificationService notificationService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final DefaultRoleService defaultRoleService;
    private final KeysRepository keysRepository;
    private final KeyUtility keyUtility;
    private final BusinessRepository businessRepository;

    @Value("${test.key.prefix}")
    String testPrefix;

    @Value("${live.key.prefix}")
    String livePrefix;


    @Override
    public Mono<Response<User>> twoFactorFirstLogin(LoginDTO request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        }catch (Exception e){
            if (e instanceof DisabledException){
                log.error(e.getMessage());
                return Mono.just(Response.<User>builder()
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .status(HttpStatus.UNAUTHORIZED)
                        .errors(List.of("User is disabled, contact support@vestrapay.com to enable user"))
                        .build());
            }
            else {
                return Mono.just(Response.<User>builder()
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .status(HttpStatus.UNAUTHORIZED)
                        .errors(List.of(e.getMessage()))
                        .build());
            }

        }


        return userRepository.findUserByEmailAndEnabled(request.getEmail(),true)
                .flatMap(user -> {
                    Object otp = redisUtility.getValue(request.getEmail() + "_OTP");
                    if (otp==null){
                        log.error("Expired OTP");
                        return Mono.just(Response.<User>builder()
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .message(FAILED)
                                .errors(List.of("Expired OTP. kindly regenerate"))
                                .build());
                    }

                    if (!request.getOtp().equalsIgnoreCase(otp.toString())){
                        log.error("incorrect OTP");
                        return Mono.just(Response.<User>builder()
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .message(FAILED)
                                .errors(List.of("Incorrect OTP"))
                                .build());
                    }



                    var token = jwtService.generateToken(user);
                    notificationService.sendEmailAsync(EmailDTO.builder()
                            .body(String.format("Dear %s %s %n A successful login into your account was made.",user.getFirstName(),user.getLastName()))
                            .subject("LOGIN NOTIFICATION")
                            .to(user.getEmail())
                            .build()).subscribe();
                    return Mono.just(Response.<User>builder()
                            .data(user)
                            .statusCode(HttpStatus.OK.value())
                            .status(HttpStatus.OK)
                            .message(token)
                            .build());
                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not found with email or not enabled  {} contact support@vestrapay.com to enable user",request.getEmail());
                    return Mono.just(Response.<User>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .errors(List.of("User not found with email "+request.getEmail()))
                            .message("User not found with email "+request.getEmail())
                            .build());
                })).doOnError(throwable -> {
                    log.error("error occurred logging in. error is {}",throwable.getMessage());
                    throw new CustomException(Response.<String>builder().build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<LoginResponseDTO>> login(FirstLoginDTO request) {
        return userRepository.findUserByEmail(request.getEmail())
                .flatMap(user -> {
                    if (!user.isEnabled()){
                        return Mono.just(Response.<LoginResponseDTO>builder()
                                .message(FAILED)
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .errors(List.of("user not enabled. verify OTP"))
                                .build());
                    }

                    var token = jwtService.generateToken(user);
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
                        log.error("incorrect password");
                        return Mono.just(Response.<LoginResponseDTO>builder()
                                .message(FAILED)
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .errors(List.of("Incorrect Password"))
                                .build());
                    }
                    String otp = otpUtils.generateOTP(6);
                    redisUtility.removeValue(request.getEmail()+"_OTP");

                    //all admins must have 2FA by default
                    if (user.getUserType().equals(UserType.ADMIN)||user.getUserType().equals(UserType.SUPER_ADMIN)){
                        log.info("admin login otp for user {} is {}",request.getEmail(),otp);
                        redisUtility.setValue(request.getEmail()+"_OTP",otp,10);
                        notificationService.sendEmailAsync(EmailDTO.builder()
                                .body("LOGIN OTP :: "+otp)
                                .subject("LOGIN OTP")
                                .to(user.getEmail())
                                .build()).subscribe();

                        return Mono.just(Response.<LoginResponseDTO>builder()
                                .data(LoginResponseDTO.builder()
                                        .enabled(true)
                                        .build())
                                .status(HttpStatus.OK)
                                .statusCode(HttpStatus.OK.value())
                                .build());
                    }

                    else {
                        return businessRepository.findBusinessByMerchantId(user.getMerchantId())
                                .flatMap(business -> {
                                    if (business.isTwoFAlogin()){
                                        log.info("login otp for user {} is {}",request.getEmail(),otp);
                                        redisUtility.setValue(request.getEmail()+"_OTP",otp,10);
                                        notificationService.sendEmailAsync(EmailDTO.builder()
                                                .body("LOGIN OTP :: "+otp)
                                                .subject("LOGIN OTP")
                                                .to(user.getEmail())
                                                .build()).subscribe();

                                        return Mono.just(Response.<LoginResponseDTO>builder()
                                                .data(LoginResponseDTO.builder()
                                                        .enabled(true)
                                                        .build())
                                                        .message(null)
                                                .status(HttpStatus.OK)
                                                .statusCode(HttpStatus.OK.value())
                                                .build());
                                    }
                                    else {
                                        notificationService.sendEmailAsync(EmailDTO.builder()
                                                .body(String.format("Dear %s %s %n A successful login into your account was made.",user.getFirstName(),user.getLastName()))
                                                .subject("LOGIN NOTIFICATION")
                                                .to(user.getEmail())
                                                .build()).subscribe();
                                        return Mono.just(Response.<LoginResponseDTO>builder()
                                                        .message(token)
                                                .data(LoginResponseDTO.builder()
                                                        .enabled(false)
                                                        .user(user)
                                                        .build())
                                                .status(HttpStatus.OK)
                                                .statusCode(HttpStatus.OK.value())
                                                .build());
                                    }

                                }).switchIfEmpty(Mono.defer(() -> {
                                    if(user.getUserType().equals(UserType.MERCHANT)){
                                        notificationService.sendEmailAsync(EmailDTO.builder()
                                                .body(String.format("Dear %s %s %n A successful login into your account was made.",user.getFirstName(),user.getLastName()))
                                                .subject("LOGIN NOTIFICATION")
                                                .to(user.getEmail())
                                                .build()).subscribe();
                                        return Mono.just(Response.<LoginResponseDTO>builder()
                                                .data(LoginResponseDTO.builder()
                                                        .user(user)
                                                        .enabled(false)
                                                        .build())
                                                        .message(token)
                                                .status(HttpStatus.OK)
                                                .statusCode(HttpStatus.OK.value())
                                                .build());
                                    }

                                    log.info("login otp for user {} is {}",request.getEmail(),otp);
                                    redisUtility.setValue(request.getEmail()+"_OTP",otp,10);
                                    notificationService.sendEmailAsync(EmailDTO.builder()
                                            .body("LOGIN OTP :: "+otp)
                                            .subject("LOGIN OTP")
                                            .to(user.getEmail())
                                            .build()).subscribe();



                                    return Mono.just(Response.<LoginResponseDTO>builder()
                                            .data(LoginResponseDTO.builder()
                                                    .enabled(true)
                                                    .build())
                                            .status(HttpStatus.OK)
                                            .statusCode(HttpStatus.OK.value())
                                            .build());
                                }));
                    }





                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not found");
                    return Mono.just(Response.<LoginResponseDTO>builder()
                                    .message(FAILED)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                                    .errors(List.of("user not found"))
                            .build());
                })).doOnError(throwable -> {
                    log.error("error fetching user with email {}",request.getEmail());
                    throw new CustomException(Response.<LoginResponseDTO>builder()
                            .errors(List.of("error fetching user with email "+request.getEmail()))
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message(FAILED)
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Boolean>> verifyOTP(VerifyOtpDTO request) {
        log.info("verify Otp request from email {} and otp {}",request.getEmail(),request.getOtp());
        Object otp = redisUtility.getValue(request.getEmail() + "_OTP");
        if (otp==null){
            log.error("OTP expired. for email {}",request.getEmail());
            return Mono.just(Response.<Boolean>builder()
                    .data(Boolean.FALSE)
                    .message(FAILED)
                    .status(HttpStatus.UNAUTHORIZED)
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .errors(List.of("Expired OTP. resend OTP"))
                    .build());

        }
        else {
            log.info("OTP gotten from Redis");
            if (otp.toString().equals(request.getOtp())){
                return userRepository.findUserByEmail(request.getEmail()).flatMap(user1 -> {
                    log.info("user OTP verified for user {}",user1.toString());
                    redisUtility.removeValue(request.getEmail() + "_OTP");

                    user1.setEnabled(true);
                    return userRepository.save(user1)
                            .flatMap(user -> {
                                defaultRoleService.createDefaultRole(user.getUuid(), user.getMerchantId(),"MERCHANT").subscribe();
                                CompletableFuture.runAsync(() -> generateMerchantKey(KeyUsage.TEST,user.getMerchantId()));
                                return Mono.just(Response.<Boolean>builder()
                                        .data(Boolean.TRUE)
                                        .message(SUCCESSFUL)
                                        .status(HttpStatus.OK)
                                        .statusCode(HttpStatus.OK.value())
                                        .build());
                            });


                }).doOnError(throwable -> {
                    log.error("error updating user with OTP. error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.<Boolean>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error updating user OTP",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
            }
            else {
                log.error("incorrect OTP provided for user {}",request.getEmail());
                return Mono.just(Response.<Boolean>builder()
                        .data(Boolean.FALSE)
                        .message(FAILED)
                        .status(HttpStatus.UNAUTHORIZED)
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .errors(List.of("Incorrect OTP provided"))
                        .build());
            }
        }
    }

    @Override
    public Mono<Response<Boolean>> resendOTP(String req) {
        String email = req.replace("\"","");
        return userRepository.findUserByEmail(email).flatMap(user -> {
            var otp = otpUtils.generateOTP(6);
            log.info("OTP fpr email {} is {}",email,otp);
            user.setOtp(otp);
            return userRepository.save(user)
                    .flatMap(user1 -> {
                        notificationService.sendEmailAsync(EmailDTO.builder()
                                        .body(otp)
                                        .to(email)
                                        .subject("OTP VERIFICATION")
                                .build()).subscribe();
                        redisUtility.removeValue(email+"_OTP");
                        redisUtility.setValue(email+"_OTP",otp,10);
                        log.info("OTP saved to redis for user {} otp {}",email,otp);
                        return Mono.just(Response.<Boolean>builder()
                                        .status(HttpStatus.OK)
                                        .statusCode(HttpStatus.OK.value())
                                        .message(SUCCESSFUL)
                                        .data(Boolean.TRUE)
                                .build());

                    }).doOnError(throwable -> {
                        log.error("error saving otp for user {}", email);
                        throw new CustomException(Response.<Boolean>builder()
                                .message(FAILED)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .errors(List.of(throwable.getLocalizedMessage(),throwable.getMessage()))
                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                    });

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("user not found with email {}",email);
            return Mono.just(Response.<Boolean>builder()
                            .errors(List.of("user not found"))
                            .message(FAILED)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                    .build());

        })).doOnError(throwable -> {
            log.error("error resending otp for user {}", email);
            throw new CustomException(Response.<Boolean>builder()
                    .message(FAILED)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errors(List.of(throwable.getLocalizedMessage(),throwable.getMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    @Override
    public Mono<User> getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findUserByEmail(authentication.getName());
    }

    @Override
    public Mono<Response<Boolean>> changePassword(ChangePasswordDTO request) {
        log.info("about changing password with DTO {}",request.toString());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findUserByEmail(authentication.getName())
                .flatMap(user -> {
                    boolean matches = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
                    if (!matches){
                        return Mono.just(Response.<Boolean>builder()
                                .message(FAILED)
                                .status(HttpStatus.CONFLICT)
                                .statusCode(HttpStatus.CONFLICT.value())
                                        .errors(List.of("Old Password Mismatch"))
                                .data(Boolean.FALSE)
                                .build());
                    }

                    boolean validPassword = isValidPassword(request.getNewPassword());
                    if (!validPassword)
                        return Mono.just(Response.<Boolean>builder()
                                .message(FAILED)
                                .status(HttpStatus.BAD_REQUEST)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .errors(List.of("Password strength check failed"))
                                .data(Boolean.FALSE)
                                .build());

                    String password = passwordEncoder.encode(request.getNewPassword());
                    user.setPassword(password);
                    return userRepository.save(user)
                            .flatMap(user1 -> {
                                log.info("user password successfully updated for user {}",user1.getEmail());
                                return Mono.just(Response.<Boolean>builder()
                                                .message(SUCCESSFUL)
                                                .status(HttpStatus.OK)
                                                .statusCode(HttpStatus.OK.value())
                                                .data(Boolean.TRUE)
                                        .build());
                            }).doOnError(throwable -> {
                                log.error("error saving updated password to db. error is {}",throwable.getLocalizedMessage());
                                throw new CustomException(Response.<String>builder()
                                        .errors(List.of("Error saving password",throwable.getLocalizedMessage()))
                                        .message("error saving password")
                                        .data(FAILED)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });

                }).switchIfEmpty(Mono.defer(()->{
                    log.error("user not found or logged in");
                    return Mono.just(Response.<Boolean>builder()
                                    .errors(List.of("User not found or logged in"))
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                                    .message(FAILED)
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error("error occurred while changing password for user");
                    throw new CustomException(Response.<Boolean>builder()
                            .errors(List.of("Error changing password",throwable.getLocalizedMessage()))
                            .message(FAILED)
                            .data(Boolean.FALSE)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Boolean>> resetPassword(String email) {
        log.info("resetting password for email {}",email);
        String newEmail = email.replace("\"","");
        return userRepository.findUserByEmail(newEmail).flatMap(user -> {
            var password = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(password));
            return userRepository.save(user)
                    .flatMap(savedUser -> {
                        notificationService.sendEmailAsync(EmailDTO.builder()
                                        .subject("PASSWORD RESET")
                                        .to(newEmail)
                                        .body(password)
                                .build()).subscribe();
                        return Mono.just(Response.<Boolean>builder()
                                .data(Boolean.TRUE)
                                .message(SUCCESSFUL)
                                .statusCode(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .build());
                    });

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("user not found for email {}",newEmail);
            return Mono.just(Response.<Boolean>builder()
                    .status(HttpStatus.NOT_FOUND)
                    .errors(List.of("User not found"))
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .data(Boolean.FALSE)
                    .build());

        })).doOnError(throwable -> {
            log.error("error occurred resetting password for email {}",newEmail);
            throw new CustomException(Response.<String>builder()
                    .errors(List.of(throwable.getLocalizedMessage()))
                    .message("error resetting password")
                    .data(FAILED)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });    }

    public void generateMerchantKey(KeyUsage keyUsage, String merchantId){
        Keys keys;
        if (keyUsage.equals(KeyUsage.TEST)){
            keys = Keys.builder()
                    .publicKey(testPrefix.concat("PubK").concat(keyUtility.generateHexKey()))
                    .secretKey(testPrefix.concat("PrvK").concat(keyUtility.generateHexKey()))
                    .encryptionKey(testPrefix.concat("encK").concat(keyUtility.generateHexKey()))
                    .build();
        }
        else {
            keys = Keys.builder()
                    .publicKey(livePrefix.concat("PubK").concat(keyUtility.generateHexKey()))
                    .secretKey(livePrefix.concat("PrvK").concat(keyUtility.generateHexKey()))
                    .encryptionKey(livePrefix.concat("encK").concat(keyUtility.generateHexKey()))
                    .build();
        }

        keys.setKeyUsage(keyUsage);
        keys.setUserId(merchantId);

        keysRepository.findByUserIdAndKeyUsage(merchantId, keyUsage.name())
                .flatMap(keys1 -> {
                    keys1.setEncryptionKey(keys.getEncryptionKey());
                    keys1.setPublicKey(keys.getPublicKey());
                    keys1.setSecretKey(keys.getSecretKey());
                    return keysRepository.save(keys1)
                            .flatMap(keys2 -> {
                                log.info("{} keys successfully generated",keyUsage.name());
                                return Mono.just(Response.<Keys>builder()
                                        .data(keys1)
                                        .message(SUCCESSFUL)
                                        .status(HttpStatus.CREATED)
                                        .statusCode(HttpStatus.CREATED.value())
                                        .build());
                            }).doOnError(throwable -> {
                                log.error("error saving {} keys, error is {}",keyUsage.name(),throwable.getLocalizedMessage());
                                throw new CustomException(Response.<User>builder()
                                        .message(FAILED)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of("error saving keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });

                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("creating new {} keys ",keyUsage.name());
                    keys.setUuid(UUID.randomUUID().toString());
                    return keysRepository.save(keys)
                            .flatMap(keys2 -> {
                                return Mono.just(Response.<Keys>builder()
                                        .data(keys2)
                                        .message(SUCCESSFUL)
                                        .status(HttpStatus.CREATED)
                                        .statusCode(HttpStatus.CREATED.value())
                                        .build());
                            }).doOnError(throwable -> {
                                log.error("error saving {} keys, error is {}",keyUsage.name(),throwable.getLocalizedMessage());
                                throw new CustomException(Response.<User>builder()
                                        .message(FAILED)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of("error saving keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });
                }))
                .doOnError(throwable -> {
                    log.error("error fetching {} keys, error is {}",keyUsage.name(),throwable.getLocalizedMessage());
                    throw new CustomException(Response.<User>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                }).subscribe();

    }

}

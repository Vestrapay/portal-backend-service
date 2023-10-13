package com.example.vestrapay.authentications.services;

import com.example.vestrapay.authentications.dtos.ChangePasswordDTO;
import com.example.vestrapay.authentications.dtos.LoginDTO;
import com.example.vestrapay.authentications.dtos.VerifyOtpDTO;
import com.example.vestrapay.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.configs.JwtService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.notifications.interfaces.INotificationService;
import com.example.vestrapay.notifications.models.EmailDTO;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.users.repository.UserRepository;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.utils.service.OtpUtils;
import com.example.vestrapay.utils.service.RedisUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class AuthenticationService implements IAuthenticationService {
    private final UserRepository userRepository;
    private final RedisUtility redisUtility;
    private final OtpUtils otpUtils;
    private final INotificationService notificationService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Mono<Response<User>> login(LoginDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        return userRepository.findUserByEmailAndEnabled(request.getEmail(),true)
                .flatMap(user -> {
                    var token = jwtService.generateToken(user);
                    return Mono.just(Response.<User>builder()
                            .data(user)
                            .statusCode(HttpStatus.OK.value())
                            .status(HttpStatus.OK)
                            .message(token)
                            .build());
                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not found with email or not enabled {}",request.getEmail());
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
    public Mono<Response<Boolean>> verifyOTP(VerifyOtpDTO request) {
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
                return userRepository.updateUserByEmail(request.getEmail()).flatMap(user1 -> {
                    log.info("user OTP verified for user {}",user1.toString());
                    redisUtility.removeValue(request.getEmail() + "_OTP");

                    return Mono.just(Response.<Boolean>builder()
                            .data(Boolean.TRUE)
                            .message(SUCCESSFUL)
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build());
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
    public Mono<Response<Boolean>> resendOTP(String email) {
        return userRepository.findUserByEmail(email).flatMap(user -> {
            var otp = otpUtils.generateOTP(6);
            user.setOtp(otp);
            return userRepository.save(user)
                    .flatMap(user1 -> {
                        notificationService.sendEmailAsync(EmailDTO.builder()
                                        .body(otp)
                                        .to(email)
                                        .subject("OTP VERIFICATION")
                                .build()).subscribe();
                        redisUtility.removeValue(email+"_OTP");
                        redisUtility.setValue(email+"_OTP",otp,300);
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
                                .message("Successful")
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
                    .data("Failed")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });    }


}

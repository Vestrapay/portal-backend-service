package com.example.vestrapay.merchant.authentications.interfaces;

import com.example.vestrapay.merchant.authentications.dtos.*;
import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

public interface IAuthenticationService {
    Mono<Response<User>> twoFactorFirstLogin(LoginDTO request);
    Mono<Response<LoginResponseDTO>> login(FirstLoginDTO request);
    Mono<Response<Boolean>> verifyOTP(VerifyOtpDTO request);
    Mono<Response<Boolean>> changePassword(ChangePasswordDTO request);
    Mono<Response<Boolean>> resetPassword(String email);
    Mono<Response<Boolean>> resendOTP(String email);
    Mono<User> getLoggedInUser();

}

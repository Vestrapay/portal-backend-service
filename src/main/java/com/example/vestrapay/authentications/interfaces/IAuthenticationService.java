package com.example.vestrapay.authentications.interfaces;

import com.example.vestrapay.authentications.dtos.ChangePasswordDTO;
import com.example.vestrapay.authentications.dtos.LoginDTO;
import com.example.vestrapay.authentications.dtos.VerifyOtpDTO;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

public interface IAuthenticationService {
    Mono<Response<User>> login(LoginDTO request);
    Mono<Response<Boolean>> verifyOTP(VerifyOtpDTO request);
    Mono<Response<Boolean>> changePassword(ChangePasswordDTO request);
    Mono<Response<Boolean>> resetPassword(String email);
    Mono<Response<Boolean>> resendOTP(String email);
    Mono<User> getLoggedInUser();

}

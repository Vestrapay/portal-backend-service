package com.example.vestrapay.authentications.controllers;

import com.example.vestrapay.authentications.dtos.ChangePasswordDTO;
import com.example.vestrapay.authentications.dtos.LoginDTO;
import com.example.vestrapay.authentications.dtos.VerifyOtpDTO;
import com.example.vestrapay.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/auth")
@Tag(name = "AUTH", description = "Authentication Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthenticationController {
    private final IAuthenticationService authenticationService;

    @PostMapping("login")
    public Mono<ResponseEntity<Response<User>>> login(@RequestBody LoginDTO request){
        return authenticationService.login(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }
    @PostMapping("verify-otp")
    public Mono<ResponseEntity<Response<Boolean>>> verifyOTP(@RequestBody VerifyOtpDTO request){
        return authenticationService.verifyOTP(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("resend-otp")
    public Mono<ResponseEntity<Response<Boolean>>> resendOTP(@RequestBody String email){
        return authenticationService.resendOTP(email)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("change-password")
    public Mono<ResponseEntity<Response<Boolean>>> changePassword(@RequestBody ChangePasswordDTO request){
        return authenticationService.changePassword(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("reset-password")
    public Mono<ResponseEntity<Response<Boolean>>> resetPassword(@RequestBody String email){
        return authenticationService.resetPassword(email)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    public static void main(String[] args) {
        int i = camelCase("saveChangesInTheEditor");
        System.out.println(i);
    }

    public static int camelCase(String s){
        char[] charArray = s.toCharArray();
        int count = 1;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (String.valueOf(c).equals(String.valueOf(c).toUpperCase())){
                count++;
            }
        }

        return count;
    }


}

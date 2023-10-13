package com.example.vestrapay.kyc.controller;

import com.example.vestrapay.kyc.dtos.UpdateKycDTO;
import com.example.vestrapay.kyc.interfaces.IKycService;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.Multipart;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("api/v1/kyc")
@Tag(name = "KYC",description = "KYC service management")
@RequiredArgsConstructor
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
public class KycController {
    private final IKycService kycService;
    @PostMapping("upload")
    public Mono<ResponseEntity<Response<Boolean>>> uploadDocuments(@RequestParam("files") MultipartFile[] files
    ){
        return kycService.upload(files)
                .map(booleanResponse -> ResponseEntity.status(booleanResponse.getStatus()).body(booleanResponse));
    }

    @GetMapping("update")
    public Mono<ResponseEntity<Response<List<User>>>> updateKyc(@RequestBody UpdateKycDTO request){
        return kycService.update(request)
                .map(booleanResponse -> ResponseEntity.status(booleanResponse.getStatus()).body(booleanResponse));
    }

    @RequestMapping(
            value = "/**",
            method = RequestMethod.OPTIONS
    )
    public ResponseEntity handle() {
        return new ResponseEntity(HttpStatus.OK);
    }
}

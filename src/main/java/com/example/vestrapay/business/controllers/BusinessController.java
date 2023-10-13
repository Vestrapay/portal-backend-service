package com.example.vestrapay.business.controllers;

import com.example.vestrapay.business.dtos.BusinessDTO;
import com.example.vestrapay.business.interfaces.IBusinessService;
import com.example.vestrapay.business.models.Business;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/business")
@Tag(name = "BUSINESS MANAGEMENT",description = "Business management service")
@CrossOrigin(origins = "*",maxAge = 3600)
@SecurityRequirement(name = "vestrapay")
@RequiredArgsConstructor
public class BusinessController {
    private final IBusinessService businessService;

    @PostMapping("register")
    public Mono<ResponseEntity<Response<Business>>> register(@RequestBody BusinessDTO request){
        return businessService.register(request)
                .map(businessResponse -> ResponseEntity.status(businessResponse.getStatus()).body(businessResponse));
    }

    @PostMapping("update")
    public Mono<ResponseEntity<Response<Business>>> update(@RequestBody BusinessDTO request){
        return businessService.update(request)
                .map(businessResponse -> ResponseEntity.status(businessResponse.getStatus()).body(businessResponse));
    }

    @GetMapping("view")
    public Mono<ResponseEntity<Response<Business>>> view(){
        return businessService.view()
                .map(businessResponse -> ResponseEntity.status(businessResponse.getStatus()).body(businessResponse));
    }

}

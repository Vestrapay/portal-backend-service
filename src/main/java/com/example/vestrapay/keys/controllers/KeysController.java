package com.example.vestrapay.keys.controllers;

import com.example.vestrapay.keys.enums.KeyUsage;
import com.example.vestrapay.keys.interfaces.IKeyService;
import com.example.vestrapay.keys.models.Keys;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/key")
@Tag(name = "KEYS", description = "Key Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Validated
public class KeysController {
    private final IKeyService keyService;

    @GetMapping("get-keys/{environment}")
    public Mono<ResponseEntity<Response<Keys>>> getKeys(@PathVariable("environment") String environment){
        KeyUsage keyUsage = KeyUsage.valueOf(environment);
        return keyService.viewKeys(keyUsage)
                .map(keysResponse -> ResponseEntity.status(keysResponse.getStatus()).body(keysResponse));
    }

    @GetMapping("generate-keys/{environment}")
    public Mono<ResponseEntity<Response<Keys>>> generateKeys(@PathVariable("environment") String environment){
        KeyUsage keyUsage = KeyUsage.valueOf(environment);
        return keyService.generateKey(keyUsage)
                .map(keysResponse -> ResponseEntity.status(keysResponse.getStatus()).body(keysResponse));
    }
}

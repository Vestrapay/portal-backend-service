package com.example.vestrapay.commons.banks.controller;

import com.example.vestrapay.commons.banks.model.BankList;
import com.example.vestrapay.commons.banks.service.ListBanksService;
import com.example.vestrapay.utils.dtos.PagedRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/banks")
@Tag(name = "BANKS", description = "Banks Management")
//@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Validated
public class BankController {
    private final ListBanksService banksService;
    @GetMapping
    public ResponseEntity<BankList> getBanks(){
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(banksService.getBanks());

    }
}

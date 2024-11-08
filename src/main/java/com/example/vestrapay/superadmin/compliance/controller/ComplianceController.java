package com.example.vestrapay.superadmin.compliance.controller;

import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.superadmin.compliance.dtos.ValidateKYCDTO;
import com.example.vestrapay.superadmin.compliance.interfaces.IComplianceService;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("api/v1/compliance")
@Tag(name = "COMPLIANCE", description = "Compliance Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
@RequiredArgsConstructor
public class ComplianceController {
    private final IComplianceService complianceService;

    @GetMapping("{merchant-id}")
    @PreAuthorize("hasAuthority('COMPLIANCE_PERMISSION')")
    public Mono<ResponseEntity<?>> fetchAllUploadedDocuments(@PathVariable("merchant-id")String merchantId){
        return complianceService.fetchAllDocumentsByMerchantId(merchantId)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));

    }
    @PostMapping("view-pending-kyc")
    @PreAuthorize("hasAuthority('COMPLIANCE_PERMISSION')")
    public Mono<ResponseEntity<Response<List<User>>>> viewPendingKyc()
    {
        return complianceService.fetchAllPendingApprovals()
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }



    @PostMapping("validate")
    @PreAuthorize("hasAuthority('COMPLIANCE_PERMISSION')")
    public Mono<ResponseEntity<Response<?>>> validate(@RequestBody ValidateKYCDTO request)
    {
        return complianceService.validateKYC(request)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }









}

package com.example.vestrapay.superadmin.compliance.controller;

import com.example.vestrapay.superadmin.compliance.interfaces.IComplianceService;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/compliance")
@Tag(name = "COMPLIANCE", description = "Compliance Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
@RequiredArgsConstructor
public class ComplianceController {
    private final IComplianceService complianceService;
    @GetMapping("fetch-all-documents")
    public Mono<ResponseEntity<Response<?>>>fetchAllUploadedDocuments(){
        return complianceService.fetchAllDocuments()
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));

    }



}

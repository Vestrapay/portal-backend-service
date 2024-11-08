package com.example.vestrapay.merchant.audits.controllers;

import com.example.vestrapay.merchant.audits.models.AuditEvent;
import com.example.vestrapay.merchant.audits.services.AuditLogService;
import com.example.vestrapay.utils.dtos.PagedRequest;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("api/v1/audit-log")
@Tag(name = "AUDIT_LOG", description = "Audit Log Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Validated
public class AuditLogController {
    private final AuditLogService auditLogService;
    @PostMapping
    public Mono<ResponseEntity<Response<List<AuditEvent>>>> getAuditLog(@RequestBody PagedRequest request){
        return auditLogService.getLogs(request)
                .map(listResponse -> ResponseEntity.status(listResponse.getStatus()).body(listResponse));

    }
}

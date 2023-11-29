package com.example.vestrapay.superadmin.compliance.service;

import com.example.vestrapay.superadmin.compliance.enums.ApprovalStatus;
import com.example.vestrapay.superadmin.compliance.enums.ValidationStatus;
import com.example.vestrapay.superadmin.compliance.interfaces.IComplianceService;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Service
@Slf4j
@RequiredArgsConstructor
public class ComplianceService implements IComplianceService {
    @Override
    public Mono<Response<?>> fetchAllDocuments() {
        return null;
    }

    @Override
    public Mono<Response<?>> fetchAllPendingApprovals(ApprovalStatus status) {
        return null;
    }

    @Override
    public Mono<Response<?>> validateUser(ValidationStatus status) {
        return null;
    }

    @Override
    public Mono<Response<?>> fetchAllPendingUsers(ValidationStatus status) {
        return null;
    }
}

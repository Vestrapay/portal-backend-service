package com.example.vestrapay.superadmin.compliance.interfaces;

import com.example.vestrapay.superadmin.compliance.enums.ApprovalStatus;
import com.example.vestrapay.superadmin.compliance.enums.ValidationStatus;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

public interface IComplianceService {
    Mono<Response<?>>fetchAllDocuments();
    Mono<Response<?>>fetchAllPendingApprovals(ApprovalStatus status);
    Mono<Response<?>>validateUser(ValidationStatus status);
    Mono<Response<?>>fetchAllPendingUsers(ValidationStatus status);
}

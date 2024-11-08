package com.example.vestrapay.superadmin.compliance.interfaces;

import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.superadmin.compliance.dtos.ValidateKYCDTO;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IComplianceService {
    Mono<Response<?>> fetchAllDocumentsByMerchantId(String merchantId);
    Mono<Response<List<User>>>fetchAllPendingApprovals();
    Mono<Response<Object>> validateKYC(ValidateKYCDTO request);


}

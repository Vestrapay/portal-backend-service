package com.example.vestrapay.superadmin.dispute.interfaces;

import com.example.vestrapay.superadmin.dispute.dtos.UpdateDisputeDTO;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

public interface IAdminDisputeService {
    Mono<Response<Object>>viewAll();
    Mono<Response<Object>>viewAllPending();

    Mono<Response<Object>> findDisputeById(String disputeId);

    Mono<Response<Object>> getDisputeDocuments(String disputeId);

    Mono<Response<Object>> updateDispute(UpdateDisputeDTO request);
}

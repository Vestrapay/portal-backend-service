package com.example.vestrapay.merchant.business.interfaces;

import com.example.vestrapay.merchant.business.dtos.BusinessDTO;
import com.example.vestrapay.merchant.business.models.Business;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

public interface IBusinessService {
    Mono<Response<Business>> register(BusinessDTO request);
    Mono<Response<Business>> update(BusinessDTO request);
    Mono<Response<Business>> delete(BusinessDTO request);
    Mono<Response<Business>> view();
}

package com.example.vestrapay.business.interfaces;

import com.example.vestrapay.business.dtos.BusinessDTO;
import com.example.vestrapay.business.models.Business;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IBusinessService {
    Mono<Response<Business>> register(BusinessDTO request);
    Mono<Response<Business>> update(BusinessDTO request);
    Mono<Response<Business>> delete(BusinessDTO request);
    Mono<Response<Business>> view();
}

package com.example.vestrapay.users.interfaces;

import com.example.vestrapay.superadmin.dto.AdminUserDTO;
import com.example.vestrapay.users.dtos.MerchantUserDTO;
import com.example.vestrapay.users.dtos.UserDTO;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IUserService {
    Mono<Response<Void>> createAccount(UserDTO request);
    Mono<Response<Void>> createAdminAccount(AdminUserDTO request);
    Mono<Response<Void>> createMerchantUsers(String merchantId, MerchantUserDTO request);
    Mono<Response<User>> updateMerchantUsers(String merchantId, User request);
    Mono<Response<Void>> deleteMerchantUsers(String merchantId, String userId);
    Mono<Response<List<User>>> merchantViewAllUsers();
    Mono<Response<User>> updateUser(UserDTO request);
    Mono<Response<User>> viewUser();

    Mono<Response<Void>> deleteUser(String userId);

    Mono<Response<?>>migrateToProd(String userId, String merchantId);
}

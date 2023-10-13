package com.example.vestrapay.users.controllers;

import com.example.vestrapay.users.dtos.MerchantUserDTO;
import com.example.vestrapay.users.dtos.UserDTO;
import com.example.vestrapay.users.interfaces.IUserService;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("api/v1/user")
@Tag(name = "USERS", description = "User Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @PostMapping("register")
    public Mono<ResponseEntity<Response<Void>>> createAccount(@RequestBody @Valid UserDTO request){
        return userService.createAccount(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("update")
    public Mono<ResponseEntity<Response<User>>> updateUser(@RequestBody @Valid UserDTO request){
        return userService.updateUser(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("delete")
    public Mono<ResponseEntity<Response<Void>>> deleteUser(@RequestBody @Valid String userId){
        return userService.deleteUser(userId)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @GetMapping("view-user")
    public Mono<ResponseEntity<Response<User>>> viewUser(){
        return userService.viewUser()
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("merchant-create-user")
    @PreAuthorize("hasRole('CREATE_USER')")
    public Mono<ResponseEntity<Response<Void>>> merchantCreateUser(@RequestHeader String merchantId,@RequestBody @Valid MerchantUserDTO request){
        return userService.createMerchantUsers(merchantId,request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("merchant-update-user")
    @PreAuthorize("hasRole('UPDATE_USER')")
    public Mono<ResponseEntity<Response<User>>> merchantUpdateUser(@RequestHeader String merchantId,@RequestBody @Valid User request){
        return userService.updateMerchantUsers(merchantId,request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("delete-merchant-user")
    @PreAuthorize("hasRole('DELETE_USER')")
    public Mono<ResponseEntity<Response<Void>>> merchantDeleteUser(@RequestHeader String merchantId,@RequestBody @Valid String userId){
        return userService.deleteMerchantUsers(merchantId,userId)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @GetMapping("view-all-merchant-users")
    @PreAuthorize("hasRole('VIEW_USER')")
    public Mono<ResponseEntity<Response<List<User>>>> merchantViewUsers(){
        return userService.merchantViewAllUsers()
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("view-merchant-user")
    @PreAuthorize("hasRole('VIEW_USER')")
    public Mono<ResponseEntity<Response<?>>> merchantViewUser(@RequestHeader String merchantId,@RequestBody @Valid String userId){
        //todo coming soon
        return Mono.just(ResponseEntity.ok(null));
    }

    @RequestMapping(
            value = "/**",
            method = RequestMethod.OPTIONS
    )
    public ResponseEntity handle() {
        return new ResponseEntity(HttpStatus.OK);
    }
}

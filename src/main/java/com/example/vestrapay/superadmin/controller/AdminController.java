package com.example.vestrapay.superadmin.controller;

import com.example.vestrapay.superadmin.dto.AdminUserDTO;
import com.example.vestrapay.superadmin.service.IAdminService;
import com.example.vestrapay.users.dtos.UserDTO;
import com.example.vestrapay.users.interfaces.IUserService;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("api/v1/admin")
@Tag(name = "ADMIN MANAGEMENT", description = "Admin Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
@RequiredArgsConstructor
public class AdminController{
    private final IUserService userService;
    private final IAdminService adminService;
    @PostMapping("create-admin")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>> createAdmin(@RequestBody AdminUserDTO request){

        return userService.createAdminAccount(request)
                .map(voidResponse -> ResponseEntity.status(voidResponse.getStatus()).body(voidResponse));

    }

    @GetMapping("view-all-admin")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<List<User>>>> viewAllAdmin(){

        return adminService.viewAllAdmin()
                .map(voidResponse -> ResponseEntity.status(voidResponse.getStatus()).body(voidResponse));

    }

    @GetMapping("view-all-merchants")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<List<User>>>> viewAllMerchants(){

        return adminService.viewAllMerchants()
                .map(voidResponse -> ResponseEntity.status(voidResponse.getStatus()).body(voidResponse));

    }

    @GetMapping("disable-merchant/{merchantId}")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<Boolean>>> disableMerchant(@PathVariable("merchantId")String merchantId){

        return adminService.disableMerchant(merchantId)
                .map(voidResponse -> ResponseEntity.status(voidResponse.getStatus()).body(voidResponse));

    }

    @RequestMapping(
            value = "/**",
            method = RequestMethod.OPTIONS
    )
    public ResponseEntity handle() {
        return new ResponseEntity(HttpStatus.OK);
    }


}

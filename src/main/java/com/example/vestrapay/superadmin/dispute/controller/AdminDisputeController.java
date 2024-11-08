package com.example.vestrapay.superadmin.dispute.controller;

import com.example.vestrapay.superadmin.dispute.dtos.UpdateDisputeDTO;
import com.example.vestrapay.superadmin.dispute.interfaces.IAdminDisputeService;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/admin/dispute")
@Tag(name = "ADMIN DISPUTE ", description = "Admin Dispute Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
@RequiredArgsConstructor
public class AdminDisputeController {
    private final IAdminDisputeService adminDisputeService;

    @GetMapping("view-all")
    @PreAuthorize("hasAnyAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>viewAll(){
        return adminDisputeService.viewAll()
                .map(objectResponse -> ResponseEntity.status(objectResponse.getStatus()).body(objectResponse));
    }
    @GetMapping("get-all-pending")
    @PreAuthorize("hasAnyAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>viewAllPending(){
        return adminDisputeService.viewAllPending()
                .map(objectResponse -> ResponseEntity.status(objectResponse.getStatus()).body(objectResponse));
    }

    @GetMapping("view-by-id/{disputeId}")
    @PreAuthorize("hasAnyAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>getDisputeById(@PathVariable("disputeId") String disputeId){
        return adminDisputeService.findDisputeById(disputeId)
                .map(objectResponse -> ResponseEntity.status(objectResponse.getStatus()).body(objectResponse));
    }

    @GetMapping("get-dispute-documents/{transactionReference}")
    @PreAuthorize("hasAnyAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Object>> getDisputeDocuments(@PathVariable("transactionReference") String transactionReference){
        return adminDisputeService.getDisputeDocuments(transactionReference)
                .map(response -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.setContentDispositionFormData("attachment", transactionReference.concat(".zip"));

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(response.getData());
                });
    }

    @PostMapping("update")
    @PreAuthorize("hasAnyAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>updateDispute(@RequestBody UpdateDisputeDTO request){
        return adminDisputeService.updateDispute(request)
                .map(objectResponse -> ResponseEntity.status(objectResponse.getStatus()).body(objectResponse));
    }



}

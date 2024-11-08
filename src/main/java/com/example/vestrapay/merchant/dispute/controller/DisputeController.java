package com.example.vestrapay.merchant.dispute.controller;

import com.example.vestrapay.merchant.dispute.dto.DisputeDTO;
import com.example.vestrapay.merchant.dispute.interfaces.IDisputeService;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("api/v1/dispute")
@Tag(name = "DISPUTE", description = "Dispute Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
@RequiredArgsConstructor
@Validated
public class DisputeController {
    private final IDisputeService disputeService;
    @PostMapping("log")
    public Mono<ResponseEntity<Response<?>>>logDispute(@RequestParam("reference")String reference,
                                                       @RequestParam(name="comment",required = false) String comment,
                                                       @RequestParam(name="files",required = false)MultipartFile[] files){
        DisputeDTO request = DisputeDTO.builder().transactionReference(reference).comment(comment).build();
        return disputeService.logDispute(request,files)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @GetMapping("view/{uuid}")
    public Mono<ResponseEntity<Response<?>>>viewDisputeById(@PathVariable("uuid") String uuid){
        return disputeService.viewDisputeById(uuid)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @GetMapping("view-all")
    public Mono<ResponseEntity<Response<?>>>viewAllDispute(){
        return disputeService.viewAllDispute()
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @PostMapping("view-by-param")
    public Mono<ResponseEntity<Response<?>>>viewByParam(@RequestBody Map<String,String>param
                                                        ){
        return disputeService.viewByParam(param)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }
}

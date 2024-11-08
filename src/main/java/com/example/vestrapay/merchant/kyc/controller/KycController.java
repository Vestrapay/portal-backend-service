package com.example.vestrapay.merchant.kyc.controller;

import com.example.vestrapay.merchant.kyc.dtos.UpdateKycDTO;
import com.example.vestrapay.merchant.kyc.interfaces.IKycService;
import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequestMapping("api/v1/kyc")
@Tag(name = "KYC",description = "KYC service management")
@RequiredArgsConstructor
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
public class KycController {
    private final IKycService kycService;
    @PostMapping("upload")
    public Mono<ResponseEntity<Response<Object>>> uploadDocuments(@RequestParam(name ="certificate_of_incorporation",required = false) MultipartFile[] file1,
                                                                   @RequestParam(name="register_of_shareholder",required = false)MultipartFile[] file2,
                                                                   @RequestParam(name="register_of_directors",required = false)MultipartFile[] file3,
                                                                   @RequestParam(name="memorandum_and_articles_of_association",required = false)MultipartFile[] file4,
                                                                   @RequestParam(name="valid_id_of_directors",required = false)MultipartFile[] file5,
                                                                   @RequestParam(name="valid_id_of_ultimate_beneficial_owners",required = false)MultipartFile[] file6,
                                                                   @RequestParam(name="operating_license",required = false)MultipartFile[] file7,
                                                                   @RequestParam(name="due_diligence_questionaire",required = false)MultipartFile[] file8

    ){
        Map<String, MultipartFile[]> documents = getDocuments(file1, file2, file3, file4, file5, file6, file7, file8);
        Map<String, String> map = validateFileType(documents);
        if (map.isEmpty()){
            return kycService.upload(documents).map(booleanResponse -> ResponseEntity.status(booleanResponse.getStatus()).body(booleanResponse));
        }
        else {
            Response<Object> response = new Response<>();
            response.setStatus(HttpStatus.NOT_FOUND);
            response.setMessage("Failed");
            List<String>errors = new ArrayList<>();
            map.forEach((s, s2) -> errors.add(s+" "+s2));
            response.setErrors(errors);
            return Mono.just(ResponseEntity.status(response.getStatus()).body(response));
        }

    }

    private static Map<String, MultipartFile[]> getDocuments(MultipartFile[] file1, MultipartFile[] file2, MultipartFile[] file3, MultipartFile[] file4, MultipartFile[] file5, MultipartFile[] file6, MultipartFile[] file7, MultipartFile[] file8) {
        Map<String, MultipartFile[]> documents = new HashMap<>();
        if (file1 !=null)
            documents.put("certificate_of_incorporation", file1);
        if (file2 !=null)
            documents.put("register_of_shareholder", file2);
        if (file3 !=null)
            documents.put("register_of_directors", file3);
        if (file4 !=null)
            documents.put("memorandum_and_articles_of_association", file4);
        if (file5 !=null)
            documents.put("valid_id_of_directors", file5);
        if (file6 !=null)
            documents.put("valid_id_of_ultimate_beneficial_owners", file6);
        if (file7 !=null)
            documents.put("operating_license", file7);
        if (file8 !=null)
            documents.put("due_diligence_questionaire", file8);
        return documents;
    }

    @GetMapping("update")
    public Mono<ResponseEntity<Response<List<User>>>> updateKyc(@RequestBody UpdateKycDTO request){
        return kycService.update(request)
                .map(booleanResponse -> ResponseEntity.status(booleanResponse.getStatus()).body(booleanResponse));
    }

    @RequestMapping(
            value = "/**",
            method = RequestMethod.OPTIONS
    )
    public ResponseEntity handle() {
        return new ResponseEntity(HttpStatus.OK);
    }


    private Map<String,String> validateFileType(Map<String, MultipartFile[]> files){
        Map<String,String> responseMap = new HashMap<>();
        files.forEach((s, files1) -> {
            for (MultipartFile file: files1){
                String originalFilename = file.getOriginalFilename();

                if (originalFilename != null) {
                    if (!isValidFileExtension(originalFilename)) {
                        responseMap.put(s,"extension not valid. pdf, jpg, png");

                    }
                }
            }

        });

        return responseMap;
    }
    private boolean isValidFileExtension(String filename) {
        String extension = getFileExtension(filename);
        return "pdf".equalsIgnoreCase(extension) || "jpg".equalsIgnoreCase(extension);
    }

    // Helper method to extract the file extension
    private String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return ""; // No extension found
        }
        return filename.substring(lastIndexOfDot + 1);
    }
}

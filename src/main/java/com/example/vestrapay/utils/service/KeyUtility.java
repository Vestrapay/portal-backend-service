package com.example.vestrapay.utils.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KeyUtility {
    public String generateHexKey(){
        return UUID.randomUUID().toString().replace("-","").toUpperCase();
    }

}

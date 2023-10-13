package com.example.vestrapay.utils.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class OtpUtils {
    public String generateOTP(int size){
        return RandomStringUtils.randomNumeric(size);
    }
}

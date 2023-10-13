package com.example.vestrapay.utils.dtos;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    public static final String FAILED  = "FAILED";
    public static final String SUCCESSFUL  = "SUCCESSFUL";
    public static final String USER_NOT_LOGGED_IN  = "user not logged in";
    public static final String ERROR_FETCHING_USER  = "error fetching logged in user";
}

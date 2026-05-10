package com.smart_hire.auth.controller;

public final class AuthApiPaths {

    public static final String BASE_PATH = "/api/auth";
    public static final String REGISTER_PATH = "/register";
    public static final String REGISTER_ENDPOINT = BASE_PATH + REGISTER_PATH;
    public static final String LOGIN_PATH = "/login";
    public static final String LOGIN_ENDPOINT = BASE_PATH + LOGIN_PATH;
    public static final String VALIDATE_PATH = "/validate";
    public static final String VALIDATE_ENDPOINT = BASE_PATH + VALIDATE_PATH;
    public static final String USER_BY_ID_PATH = "/users/{id}";

    private AuthApiPaths() {
    }
}

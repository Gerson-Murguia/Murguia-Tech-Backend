package com.example.securityjwt.constant;

public class SecurityConstant {
    //CONSTANTES DE SEGURIDAD
    public static final long EXPIRATION_TIME=432_000_000; //5 dias en milisegundos
    public static final String FRONTEND_URL="https://murguiatech.netlify.app"; //no necesita verificacion el token
    public static final String TOKEN_PREFIX="Bearer "; //no necesita verificacion el token
    public static final String JWT_TOKEN_HEADER="Jwt-Token";//estara en el header del request
    public static final String TOKEN_CANNOT_BE_VERIFIED="El token no se puede verificar";//si no se puede verificar el token
    public static final String MURGUIA_TECH="Gerson Murguia Montes, Developer";
    public static final String MURGUIA_TECH_ADMINISTRATION="E-commerce";
    public static final String AUTHORITIES="authorities";
    public static final String FORBIDDEN_MESSAGE="Necesitas logearte para acceder a esta página";
    public static final String ACCESS_DENIED_MESSAGE="No tienes permiso para acceder a esta página";
    public static final String OPTIONS_HTTP_METHOD="OPTIONS";
    //public static final String[] PUBLIC_URLS={"/user/login","user/register","user/resetpassword/**","user/image/**"};
    public static final String[] PUBLIC_URLS={"/user/login","/user/register","/user/image/**"};
}

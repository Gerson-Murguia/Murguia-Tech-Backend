package com.example.securityjwt.exception.domain;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.securityjwt.model.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;


@Slf4j
@RestControllerAdvice
public class ExceptionHandling implements  ErrorController{

    public static final String ACCOUNT_LOCKED="Tu cuenta ha sido bloqueada. Por favor, contacta a la administración";
    public static final String METHOD_IS_NOT_ALLOWED="El metodo no esta permitido en este endpoint. Por favor, envia un '%s' request";
    public static final String INTERNAL_SERVER_ERROR_MSG="Un error ocurrio durante el procesado de la solicitud";
    public static final String INCORRECT_CREDENTIALS="Usuario/password incorrectos. Por favor, intenta de nuevo";
    public static final String ACCOUNT_DISABLED="Tu cuenta ha sido deshabilitada. Si este es un error, contacta a la administración";
    public static final String ERROR_PROCESSING_FILE="Un error ocurrio durante el procesado del archivo";
    public static final String NOT_ENOUGH_PERMISSION="No tienes los permisos necesarios";

    //envia nuestro response personalizado em el responseentity
    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message){
        HttpResponse httpResponse=new HttpResponse(httpStatus.value(),httpStatus,httpStatus.getReasonPhrase(),message,new Date());

        return new ResponseEntity<HttpResponse>(httpResponse,httpStatus);
    }

    //se dispara para el disabledexception
    @ExceptionHandler(DisabledException.class)
    private ResponseEntity<HttpResponse> accountDisabledException(){
        return createHttpResponse(HttpStatus.BAD_REQUEST,ACCOUNT_DISABLED);
    }

    @ExceptionHandler(LockedException.class)
    private ResponseEntity<HttpResponse> accountLockedException(){
        return createHttpResponse(HttpStatus.UNAUTHORIZED,ACCOUNT_LOCKED);
    }

    @ExceptionHandler(InternalError.class)
    private ResponseEntity<HttpResponse> internalServerErrorException(){
        return createHttpResponse(HttpStatus.BAD_REQUEST,ACCOUNT_DISABLED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    private ResponseEntity<HttpResponse> incorrectCredentialsException(){
        return createHttpResponse(HttpStatus.BAD_REQUEST,INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    private ResponseEntity<HttpResponse> accessDeniedException(){
        return createHttpResponse(HttpStatus.BAD_REQUEST, NOT_ENOUGH_PERMISSION);
    }

    @ExceptionHandler(NotAnImageException.class)
    private ResponseEntity<HttpResponse> notAnImageException(NotAnImageException exception){
        return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    private ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception){
        return createHttpResponse(HttpStatus.UNAUTHORIZED,exception.getMessage());
    }

    @ExceptionHandler(EmailExistsException.class)
    private ResponseEntity<HttpResponse> emailExistsException(EmailExistsException ex){
        return createHttpResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
    }
    @ExceptionHandler(UsernameExistsException.class)
    private ResponseEntity<HttpResponse> usernameExistsException(UsernameExistsException ex){
        return createHttpResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
    }
    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException ex){
        return createHttpResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
    }

/* Desactivar whitelabel error page
   @ExceptionHandler(NoHandlerFoundException.class)
    private ResponseEntity<HttpResponse> serviceException(NoHandlerFoundException ex){
        return createHttpResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
    }*/

    @ExceptionHandler(EmailNotFoundException.class)
    private ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException ex){
        return createHttpResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    private ResponseEntity<HttpResponse> HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception){
        HttpMethod supportedMethods = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
        return createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED,String.format(METHOD_IS_NOT_ALLOWED,supportedMethods.toString()));
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<HttpResponse> internalServerErrorException(Exception ex){
        log.error(ex.getMessage());
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,INTERNAL_SERVER_ERROR_MSG);
    }

    @ExceptionHandler(NoResultException.class)
    private ResponseEntity<HttpResponse> notFoundException(NoResultException exception){
        log.error(exception.getMessage());
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,INTERNAL_SERVER_ERROR_MSG);
    }

    @ExceptionHandler(IOException.class)
    private ResponseEntity<HttpResponse> IOException(IOException exception){
        log.error(exception.getMessage());
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,ERROR_PROCESSING_FILE);
    }

    //handle exception to /error, se implementa ErrorController para ello
    @RequestMapping("${server.error.path}")
    private ResponseEntity<HttpResponse> notFound404(){
        return createHttpResponse(HttpStatus.NOT_FOUND,"No hay mapeo para esta url");
    }

}

package com.example.securityjwt.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Date;

import static javax.security.auth.callback.ConfirmationCallback.OK;

//Custom respuesta que se manda al usuario cada vez

@Getter
@Setter
@AllArgsConstructor
public class HttpResponse {
    private int httpStatusCode;//200:ok,300:redireccion,400:error del cliente,500: error del servidor
    private HttpStatus httpStatus;
    private String reason;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "MM-dd-yyyy hh:mm:ss",timezone = "America/Lima")
    private Date timestamp;

    public HttpResponse() {

    }
}

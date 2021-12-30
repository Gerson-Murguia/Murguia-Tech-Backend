package com.example.securityjwt.filter;

import com.example.securityjwt.constant.SecurityConstant;
import com.example.securityjwt.model.HttpResponse;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {
    //EXPLAIN: Si no esta autenticado dara 403 forbidden
    //TODO:testear la response 403

    //Siempre retorna un error code 403 al cliente
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        HttpResponse httpResponse=new HttpResponse(FORBIDDEN.value(),FORBIDDEN,FORBIDDEN.getReasonPhrase().toUpperCase(), SecurityConstant.FORBIDDEN_MESSAGE, new Date());

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(FORBIDDEN.value());
        OutputStream outputStream=response.getOutputStream();
        ObjectMapper mapper=new ObjectMapper();

        //java object a json
        mapper.writeValue(outputStream,httpResponse);
        outputStream.flush();

        response.sendError(403, "Access Denied");
    }
}

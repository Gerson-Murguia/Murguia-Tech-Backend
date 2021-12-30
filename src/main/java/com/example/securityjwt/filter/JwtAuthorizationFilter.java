package com.example.securityjwt.filter;

import com.example.securityjwt.constant.SecurityConstant;
import com.example.securityjwt.utility.JWTTokenProvider;
import lombok.RequiredArgsConstructor;

import static com.example.securityjwt.constant.SecurityConstant.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    //EXPLAIN: Se disparara cuando haya una nueva request solo una vez, y manejara la authentication
    // segun el estado del token jwt


    //cuando se use el filter, se creara un token provider
    private final JWTTokenProvider jwtTokenProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //EXPLAIN: si el metodo del request es option, no se hace nada solo se pone el status OK
        //INFO: primero se envia un option request, para ver si el request esta permitido, si lo esta se hara.
        //  antes de hacer un Put o un Post se hara un Option request
        if (request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)){
            response.setStatus(HttpStatus.OK.value());
        }else{
            //se obtiene el header de autorizacion
            String authorizationHeader = request.getHeader(AUTHORIZATION);

            //si no tiene el token jwt en el header no se hace nada
            //INFO:  si el header no comienza con nuestro prefijo "bearer " o es null
            //  sabemos que no es nuestro header de autorizacion, asi que se termina el filtro
            if (authorizationHeader==null|| !authorizationHeader.startsWith(TOKEN_PREFIX)){
                //lo pasa al siguiente filtro
                filterChain.doFilter(request,response);
                return;
            }

            //el header menos "bearer " es el token jwt
            String token=authorizationHeader.substring(TOKEN_PREFIX.length());

            //obtiene el username del token jwt
            String username=jwtTokenProvider.getSubject(token);

            //si el token es valido, debemos autenticarnos en el securitycontext pasando un authentication
            if (jwtTokenProvider.isTokenValid(username,token)){
                List<GrantedAuthority> authorities=jwtTokenProvider.getAuthorities(token);
                Authentication authentication=jwtTokenProvider.getAuthentication(username,authorities,request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else{
                //si no tiene token valido
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request,response);
    }



}

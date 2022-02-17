package com.example.securityjwt.utility;

import static com.example.securityjwt.constant.SecurityConstant.*;
import static java.util.Arrays.stream;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.securityjwt.model.AppUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTTokenProvider {
    //TIP:el secreto debe estar en un sitio seguro
    //se traera del application.properties
    @Value("${jwt.secret}")
    private String secret;

    //recibe impl de spring security del appUser
    public String generateJWTToken(AppUserDetails userDetails) {
        //obtener authorities y permisos de user
        String[] claims = getClaimsFromUser(userDetails);


        return JWT.create()
                //quien creo el token
                .withIssuer(MURGUIA_TECH)
                //en donde se usa
                .withAudience(MURGUIA_TECH_ADMINISTRATION)
                //fecha de emision
                .withIssuedAt(new Date())
                //para quien es el token, debe ser unico como el email
                .withSubject(userDetails.getUsername())
                //permisos del usuario del token,el nombre es string AUTHORITIES
                .withArrayClaim(AUTHORITIES,claims)
                //fecha expiracion
                .withExpiresAt(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
                //algoritmo a usar
                .sign(Algorithm.HMAC256(secret.getBytes()));
    }

    //obtener las authorities a partir del token
    public List<GrantedAuthority> getAuthorities(String token){
        //obtiene las authorities a partir del jwt token
        String[] claims = getClaimsFromToken(token);
        //retorna un list de simplegrantedauthority
        return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    //saca los permisos del token
    private String[] getClaimsFromToken(String token) {
        JWTVerifier verificador=getJWTVerifier();
        return verificador.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        JWTVerifier verificador;
        try {
            Algorithm algorithm=Algorithm.HMAC256(secret);
            verificador=JWT.require(algorithm).withIssuer(MURGUIA_TECH).build();

        }catch (JWTVerificationException ex){
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }

        return verificador;
    }

    private String[] getClaimsFromUser(AppUserDetails userDetails) {
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority grantedAuthority: userDetails.getAuthorities()){
            authorities.add(grantedAuthority.getAuthority());
        }
        return authorities.toArray(new String[0]);
    }

    //obtiene la autenticacion despues de verificar el token y que luego se seteara en el securitycontext
    //EXPLAIN: crea un objeto que implementa authentication y lo devuelve para pasarlo luego al securitycontext
    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=
                new UsernamePasswordAuthenticationToken(username,null,authorities);
        //web auth token construye los details a partir de un request
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return usernamePasswordAuthenticationToken;
    }

    //EXPLAIN: verifica que el token sea valido y que el username no sea null
    public boolean isTokenValid(String username,String token){
        JWTVerifier verificador=getJWTVerifier();

        return StringUtils.isNotEmpty(username) && !isTokenExpired(verificador,token);
    }

    //EXPLAIN: chequea si el token ha expirado
    private boolean isTokenExpired(JWTVerifier verificador, String token) {
        Date expiracion=verificador.verify(token).getExpiresAt();
        return expiracion.before(new Date());
    }

    //de quien es el token
    public String getSubject(String token){
        JWTVerifier verificador=getJWTVerifier();
        return verificador.verify(token).getSubject();
    }
}

//TIP:Buena libreria para manejo de strings: stringutils java apache commonslang3

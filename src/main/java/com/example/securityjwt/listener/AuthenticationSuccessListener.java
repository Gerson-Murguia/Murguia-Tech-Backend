package com.example.securityjwt.listener;

import com.example.securityjwt.model.AppUser;
import com.example.securityjwt.model.AppUserDetails;
import com.example.securityjwt.service.LoginAttempService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationSuccessListener {

    private final LoginAttempService loginAttempService;

    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event){
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof AppUserDetails){
            AppUserDetails user=(AppUserDetails) event.getAuthentication().getPrincipal();
            loginAttempService.evictUserFromLoginAttempCache(user.getUsername());
            log.info("intento exitoso listener");
        }

    }
}

package com.example.securityjwt.listener;

import com.example.securityjwt.service.LoginAttempService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationFailureListener {
    public final LoginAttempService loginAttempService;

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) throws ExecutionException {
        Object principal = event.getAuthentication().getPrincipal();
        log.info(principal+"");
        if (principal instanceof String){
            String username=(String) event.getAuthentication().getPrincipal();
            loginAttempService.addUserToLoginAttempCache(username);
        }
    }
}

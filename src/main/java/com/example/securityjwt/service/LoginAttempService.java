package com.example.securityjwt.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Service
public class LoginAttempService {

    public static final int  MAX_ATTEMPTS=5;
    public static final int  ATTEMPT_INCREMENT=1;

    //key user : value attempt
    private final LoadingCache<String,Integer> loginAttempCache;

    //TODO: En lugar de bloquear por username, se puede hacer por ip INVESTIGAR
    public LoginAttempService() {
        super();
        //build cache from guava expira 15 minutos despues de creado "intentelo de nuevo en x minutos"
        loginAttempCache = CacheBuilder.newBuilder().expireAfterWrite(15,MINUTES)
                .maximumSize(100).build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        //valor default
                        return 0;
                    }
                });
    }

    public void evictUserFromLoginAttempCache(String username){
        //remueve el key value del usuario
        loginAttempCache.invalidate(username);
    }

    public void addUserToLoginAttempCache(String username) {
        int attempt = 0;
        try {
            attempt=ATTEMPT_INCREMENT+loginAttempCache.get(username);

            loginAttempCache.put(username,attempt);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean exceedMaxAttempts(String username){
        try {
            log.info(loginAttempCache.get(username)+"");
            return loginAttempCache.get(username)>=MAX_ATTEMPTS;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }
}

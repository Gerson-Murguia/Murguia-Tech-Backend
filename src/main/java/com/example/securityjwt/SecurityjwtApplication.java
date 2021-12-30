package com.example.securityjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;

import static com.example.securityjwt.constant.FileConstant.USER_FOLDER;

@SpringBootApplication
public class SecurityjwtApplication {

    public static void main(String[] args) {

        SpringApplication.run(SecurityjwtApplication.class, args);

        new File(USER_FOLDER).mkdirs();
    }
}

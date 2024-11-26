package com.example.springrestful.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationPropertiesLogger implements ApplicationRunner {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String hibernateDdlAuto;

    @Value("${jwt.access-token.expiration}")
    private String jwtAccessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private String jwtRefreshTokenExpiration;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private String mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${application.frontend.url}")
    private String frontendUrl;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("==== Application Properties ====");
        System.out.println("Application Name: " + applicationName);
        System.out.println("Datasource URL: " + datasourceUrl);
        System.out.println("Datasource Username: " + datasourceUsername);
        System.out.println("Hibernate DDL Auto: " + hibernateDdlAuto);
        System.out.println("JWT Access Token Expiration: " + jwtAccessTokenExpiration);
        System.out.println("JWT Refresh Token Expiration: " + jwtRefreshTokenExpiration);
        System.out.println("Mail Host: " + mailHost);
        System.out.println("Mail Port: " + mailPort);
        System.out.println("Mail Username: " + mailUsername);
        System.out.println("Mail Password: " + mailPassword);
        System.out.println("Frontend URL: " + frontendUrl);
        System.out.println("===============================");
    }
}

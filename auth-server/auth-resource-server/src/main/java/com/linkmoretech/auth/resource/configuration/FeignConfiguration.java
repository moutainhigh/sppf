package com.linkmoretech.auth.resource.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

/**
 * @Author: alec
 * Description:
 * @date: 18:32 2019-06-25
 */

@Slf4j
public class FeignConfiguration implements RequestInterceptor {

    private final String AUTHORIZATION = "Authorization";

    private final String BEARER = "bearer";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        log.info("feign 调用被拦截，需要添加token");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
            OAuth2AuthenticationDetails oAuth2AuthenticationDetails = (OAuth2AuthenticationDetails)
                    authentication.getDetails();
            log.info("auth {}", oAuth2AuthenticationDetails.getTokenValue());
            requestTemplate.header(AUTHORIZATION, String.format("%s %s", BEARER, oAuth2AuthenticationDetails.getTokenValue()));
        }
    }
}

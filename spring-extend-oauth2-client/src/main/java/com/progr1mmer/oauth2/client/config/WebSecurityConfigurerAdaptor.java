package com.progr1mmer.oauth2.client.config;


import com.progr1mmer.security.access.ExtendPathDecide;
import com.progr1mmer.security.config.ExtendPathInterceptConfig;
import com.progr1mmer.security.userdetails.ExtendUser;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Suxy
 * @date 2019/10/22
 * @description http://localhost:8081/oauth/authorize?client_id=test2&redirect_uri=http://localhost:8082/login&response_type=code&state=4DU4fr
 */
@EnableOAuth2Sso
@EnableWebSecurity
public class WebSecurityConfigurerAdaptor extends WebSecurityConfigurerAdapter {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private ExtendPathInterceptConfig config;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        //访问控制
        http.authorizeRequests().antMatchers(config.getAuthPath()).access("@extendPathDecide.decide(request)");
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilterAfter(oAuth2ClientAuthenticationProcessingFilter(), RequestCacheAwareFilter.class);
        http.logout().logoutUrl("/logout").permitAll().invalidateHttpSession(true);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(config.getIgnoringPath());
    }

    @Bean
    ExtendPathDecide extendPathDecide() {
        return new ExtendPathDecide();
    }

    @Bean
    OAuth2ClientAuthenticationProcessingFilter oAuth2ClientAuthenticationProcessingFilter() {
        OAuth2RestOperations restTemplate = (this.applicationContext.getBean(UserInfoRestTemplateFactory.class)).getUserInfoRestTemplate();
        ResourceServerTokenServices tokenServices = this.applicationContext.getBean(ResourceServerTokenServices.class);
        OAuth2SsoProperties sso = this.applicationContext.getBean(OAuth2SsoProperties.class);
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(sso.getLoginPath());
        filter.setRestTemplate(restTemplate);
        filter.setTokenServices(tokenServices);
        filter.setApplicationEventPublisher(this.applicationContext);
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        return filter;
    }

    @SuppressWarnings("unchecked")
    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (httpServletRequest, httpServletResponse, authentication) -> {
            OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)authentication;
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) oAuth2Authentication.getUserAuthentication();
            Map<String, Object> details = (Map<String, Object>) token.getDetails();
            String username = (String) details.get("username");
            boolean enabled = (boolean)details.get("enabled");
            boolean accountNonExpired = (boolean)details.get("accountNonExpired");
            boolean credentialsNonExpired = (boolean)details.get("credentialsNonExpired");
            boolean accountNonLocked = (boolean)details.get("accountNonLocked");
            Collection<Map<String, String>> maps = (Collection<Map<String, String>>) details.get("authorities");
            List<GrantedAuthority> authorities = new ArrayList<>();
            maps.forEach(map -> map.values().forEach(val -> authorities.add(new SimpleGrantedAuthority(val))));
            String id = (String) details.get("id");
            boolean root = (boolean)details.get("root");
            Map<String, String> resources = (Map<String, String>) details.get("resources");
            ExtendUser extendUser = new ExtendUser(username, "", enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id, root);
            extendUser.setResources(resources);
            String next = "/";
            RequestCache requestCache = new HttpSessionRequestCache();
            SavedRequest savedRequest = requestCache.getRequest(httpServletRequest, httpServletResponse);
            if (savedRequest != null) {
                next = ((DefaultSavedRequest)savedRequest).getRequestURI();
                String redirectUrl = savedRequest.getRedirectUrl();
                if (redirectUrl.contains("?")) {
                    next += redirectUrl.substring(redirectUrl.indexOf("?"));
                }
            }
            httpServletRequest.getSession().setAttribute("user", extendUser);
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            httpServletResponse.getWriter().write("{\"status\": 0, \"msg\": \"" + next + "\"}");
        };
    }

}

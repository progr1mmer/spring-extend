package com.progr1mmer.security.config;

import com.progr1mmer.security.access.ExtendPathDecide;
import com.progr1mmer.security.userdetails.ExtendUser;
import com.progr1mmer.security.userdetails.jdbc.ExtendUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Suxy
 * @date 2019/10/22
 * @description file description
 */
@EnableWebSecurity
public class WebSecurityConfigurerAdaptor extends WebSecurityConfigurerAdapter {

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private ExtendPathInterceptConfig config;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        //访问控制
        http.authorizeRequests().antMatchers(config.getAuthPath()).access("@extendPathDecide.decide(request)");
        http.authorizeRequests().antMatchers("/login/sms").permitAll();
        http.authorizeRequests().anyRequest().authenticated();
        //登录
        http.formLogin().loginPage("/login").permitAll()
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler());
        //授权
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        //退出
        http.logout().logoutUrl("/logout").permitAll().invalidateHttpSession(true);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(config.getIgnoringPath());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        ExtendUserDetailsService userDetailsService = new ExtendUserDetailsService();
        userDetailsService.setJdbcTemplate(jdbcTemplate);
        auth.userDetailsService(userDetailsService).passwordEncoder(ExtendUser.PASSWORD_ENCODER);
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (httpServletRequest, httpServletResponse, authentication) -> {
            ExtendUser principal = (ExtendUser)authentication.getPrincipal();
            //非超级管理员则加载资源
            if (!principal.isRoot()) {
                String defRoleResourceByRoleCode = "select rr.resource, re.url from auth_role_resource rr, auth_resource re where rr.resource = re.code and rr.role in (%s)";
                //加载用户资源
                StringBuilder roles = new StringBuilder();
                principal.getAuthorities().forEach(grantedAuthority -> roles.append("\'").append(grantedAuthority.getAuthority()).append("\'").append(","));
                String sql = String.format(defRoleResourceByRoleCode, roles.substring(0, roles.length() - 1));
                List<Map<String, Object>> roleResourcesList = jdbcTemplate.queryForList(sql);
                Map<String, String> roleResourcesMap = new HashMap<>();
                roleResourcesList.forEach(item -> {
                    String resource = item.get("resource") != null ? (String) item.get("resource") : "";
                    String url = item.get("url") != null ? (String) item.get("url") : "";
                    roleResourcesMap.put(resource, url);
                });
                principal.setResources(roleResourcesMap);
            }
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
            httpServletRequest.getSession().setAttribute("user", principal);
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            httpServletResponse.getWriter().write("{\"status\": 0, \"msg\": \"" + next + "\"}");
        };
    }

    private AuthenticationFailureHandler authenticationFailureHandler() {
        return (httpServletRequest, httpServletResponse, e) -> {
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            if (e instanceof BadCredentialsException) {
                httpServletResponse.getWriter().write("{\"status\": -1, \"msg\": \"账号或密码错误\"}");
            } else {
                httpServletResponse.getWriter().write("{\"status\": -1, \"msg\": \"账号状态异常，请联系管理员\"}");
            }
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (httpServletRequest, httpServletResponse, e) -> {
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            httpServletResponse.getWriter().write("{\"status\": -1, \"msg\": \"无权访问\"}");
        };
    }

    @Bean
    ExtendPathDecide extendPathDecide() {
        return new ExtendPathDecide();
    }
}

package com.progr1mmer.oauth2.server.config;

import com.progr1mmer.security.userdetails.ExtendUser;
import com.progr1mmer.security.userdetails.jdbc.ExtendUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.annotation.Resource;

/**
 * @author Suxy
 * @date 2019/11/1
 * @description file description
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfigurerAdaptor extends AuthorizationServerConfigurerAdapter {

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients().tokenKeyAccess("permitAll()").checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(jdbcTemplate.getDataSource());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager);
        endpoints.tokenStore(new JdbcTokenStore(jdbcTemplate.getDataSource()));
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        ExtendUserDetailsService userDetailsService = new ExtendUserDetailsService();
        userDetailsService.setJdbcTemplate(jdbcTemplate);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(ExtendUser.PASSWORD_ENCODER);
        return daoAuthenticationProvider;
    }

}

package com.progr1mmer.oauth2.server.config;


import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


/**
 * @author Suxy
 * @date 2019/10/22
 * @description file description
 */
@EnableWebSecurity
public class WebSecurityConfigurerAdaptor extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        //将登录相关url交给security拦截
        http.requestMatchers().antMatchers("/login", "/login/sms", "/logout", "/oauth/authorize");
        http.authorizeRequests().antMatchers("/login/sms").permitAll();
        http.authorizeRequests().anyRequest().authenticated();
        http.formLogin().permitAll();
        //自定义登录页面
        //http.formLogin().loginPage("/login").permitAll();
        http.logout().logoutUrl("/logout").permitAll().invalidateHttpSession(true);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/**/*.ico");
    }
}

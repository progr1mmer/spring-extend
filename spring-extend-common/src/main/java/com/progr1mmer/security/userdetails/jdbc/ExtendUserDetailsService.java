package com.progr1mmer.security.userdetails.jdbc;

import com.progr1mmer.security.common.ExtendConstant;
import com.progr1mmer.security.userdetails.ExtendUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * @author Suxy
 * @date 2019/10/23
 * @description file description
 */
public class ExtendUserDetailsService extends JdbcDaoImpl {

    public ExtendUserDetailsService() {
        this.setUsersByUsernameQuery("select username,password,enabled,id,root from auth_user where username = ? or phone = ?");
        this.setAuthoritiesByUsernameQuery("select username,role from auth_user_role where username = ?");
    }

    /**
     * 增加id和是否超级管理员字段
     * @param username
     * @return
     */
    @Override
    protected List<UserDetails> loadUsersByUsername(String username) {
        return this.getJdbcTemplate().query(super.getUsersByUsernameQuery(), new String[]{username, username}, (rs, rowNum) -> {
            String user = rs.getString(1);
            String password = rs.getString(2);
            //密码以短信标志前缀开头则判定为短信登录
            HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            if (request.getParameter("password").startsWith(ExtendConstant.SMS_PASSWORD_PREFIX)) {
                if (request.getSession().getAttribute(ExtendConstant.SMS_ATTRIBUTE_NAME) != null) {
                    password = (String) request.getSession().getAttribute(ExtendConstant.SMS_ATTRIBUTE_NAME);
                }
            }
            boolean enabled = rs.getBoolean(3);
            String id = rs.getString(4);
            boolean root = rs.getBoolean(5);
            return new ExtendUser(user, password, enabled, true, true, true, AuthorityUtils.NO_AUTHORITIES, id, root);
        });
    }

    /**
     * 解决默认配置下权限为空无法登录的问题
     * @param username
     * @return
     */
    @Override
    protected List<GrantedAuthority> loadUserAuthorities(String username) {
        List<GrantedAuthority> authorities = super.loadUserAuthorities(username);
        if (authorities.isEmpty()) {
            return Collections.singletonList(new SimpleGrantedAuthority("no_auth"));
        }
        return authorities;
    }

    /**
     * 此处为真正返回到认证上下文的授权信息，需要返回我们自己定义的授权信息
     * @param username
     * @param userFromUserQuery
     * @param combinedAuthorities
     * @return
     */
    @Override
    protected UserDetails createUserDetails(String username, UserDetails userFromUserQuery, List<GrantedAuthority> combinedAuthorities) {
        String returnUsername = userFromUserQuery.getUsername();
        if (!isUsernameBasedPrimaryKey()) {
            returnUsername = username;
        }
        String id = ((ExtendUser)userFromUserQuery).getId();
        boolean root = ((ExtendUser)userFromUserQuery).isRoot();
        return new ExtendUser(returnUsername, userFromUserQuery.getPassword(), userFromUserQuery.isEnabled(), userFromUserQuery.isAccountNonExpired(), userFromUserQuery.isCredentialsNonExpired(), userFromUserQuery.isAccountNonLocked(), combinedAuthorities, id, root);
    }

}

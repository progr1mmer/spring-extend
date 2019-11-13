package com.progr1mmer.security.userdetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Map;

/**
 * @author Suxy
 * @date 2019/10/23
 * @description file description
 */
public class ExtendUser extends User {

    public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final String id;
    private final boolean root;
    private Map<String, String> resources;

    public ExtendUser(String username, String password, Collection<? extends GrantedAuthority> authorities, String id, boolean root) {
        super(username, password, authorities);
        this.id = id;
        this.root = root;
    }

    public ExtendUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, String id, boolean root) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
        this.root = root;
    }

    public String getId() {
        return id;
    }

    public boolean isRoot() {
        return root;
    }

    public Map<String, String> getResources() {
        return resources;
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }

    public boolean hasPermission(String code) {
        if (root) {
            return true;
        }
        for (String s : resources.keySet()) {
            if (s.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

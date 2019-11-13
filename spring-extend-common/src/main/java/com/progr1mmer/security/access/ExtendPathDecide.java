package com.progr1mmer.security.access;

import com.progr1mmer.security.userdetails.ExtendUser;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Suxy
 * @date 2019/11/13
 * @description file description
 */
public class ExtendPathDecide {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    public boolean decide(HttpServletRequest request) {
        Object principal = request.getSession().getAttribute("user");
        if (principal instanceof ExtendUser) {
            if (((ExtendUser) principal).isRoot()) {
                return true;
            }
            for (String url : ((ExtendUser) principal).getResources().values()) {
                if (antPathMatcher.match(url, request.getRequestURI())) {
                    return true;
                }
            }
        }
        return false;
    }

}

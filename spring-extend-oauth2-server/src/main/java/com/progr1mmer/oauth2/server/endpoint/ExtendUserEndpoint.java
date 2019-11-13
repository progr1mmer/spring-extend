package com.progr1mmer.oauth2.server.endpoint;

import com.progr1mmer.security.userdetails.ExtendUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Suxy
 * @date 2019/11/12
 * @description file description
 */
@RestController
public class ExtendUserEndpoint {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 此接口针对oauth2-client的单点登录，实际应用中最好只提供给内部调用
     * @param authentication
     * @return
     */
    @RequestMapping(value = "/oauth/user", method = RequestMethod.GET)
    public Object getUser(Authentication authentication) {
        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) authentication;
        Object principal = oAuth2Authentication.getPrincipal();
        //非超级管理员则加载资源
        if (principal instanceof ExtendUser && !((ExtendUser) principal).isRoot()) {
            String defRoleResourceByRoleCode = "select rr.resource, re.url from auth_role_resource rr, auth_resource re where rr.resource = re.code and rr.role in (%s) and re.id in (%s)";
            //加载用户资源
            StringBuilder roles = new StringBuilder();
            StringBuilder resourceIds = new StringBuilder();
            ((ExtendUser) principal).getAuthorities().forEach(grantedAuthority -> roles.append("\'").append(grantedAuthority.getAuthority()).append("\'").append(","));
            oAuth2Authentication.getOAuth2Request().getResourceIds().forEach(resourceId -> resourceIds.append("\'").append(resourceId).append("\'").append(","));
            String sql = String.format(defRoleResourceByRoleCode, roles.substring(0, roles.length() - 1), resourceIds.substring(0, roles.length() - 1));
            List<Map<String, Object>> roleResourcesList = jdbcTemplate.queryForList(sql);
            Map<String, String> roleResourcesMap = new HashMap<>();
            roleResourcesList.forEach(item -> {
                String resource = item.get("resource") != null ? (String) item.get("resource") : "";
                String url = item.get("url") != null ? (String) item.get("url") : "";
                roleResourcesMap.put(resource, url);
            });
            ((ExtendUser) principal).setResources(roleResourcesMap);
        }
        return principal;
    }

}

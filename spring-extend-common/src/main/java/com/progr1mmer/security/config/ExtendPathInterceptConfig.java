package com.progr1mmer.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Suxy
 * @date 2019/10/31
 * @description file description
 */
@Component
public class ExtendPathInterceptConfig {

    /**
     * 需要进行访问控制的URL前缀
     */
    @Value(value = "${spring.security.auth-path:/admin/**,/api/**}")
    private String [] authPath;

    /**
     * 可匿名访问的资源
     */
    @Value(value = "${spring.security.ignore-path:" +
            //图标
            "/**/*.ico," +
            //样式和js
            "/**/*.css,/**/*.js," +
            //字体图标
            "/**/*.ttf,/**/*.woff,/**/*.woff2,/**/*.eot,/**/*.svg," +
            //json文件
            "/**/*.json," +
            //图片
            "/**/*.png,/**/*.jpg,/**/*.jpeg,/**/*.gif" +
            //开放接口
            "/public/**}")
    private String [] ignoringPath;

    public String[] getAuthPath() {
        return authPath;
    }

    public String[] getIgnoringPath() {
        return ignoringPath;
    }

}

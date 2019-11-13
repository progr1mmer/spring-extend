package com.progr1mmer.oauth2.server.endpoint;

import com.progr1mmer.security.common.ExtendConstant;
import com.progr1mmer.security.userdetails.ExtendUser;
import com.progr1mmer.utils.SmsVerificationCodeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Suxy
 * @date 2019/11/13
 * @description file description
 */
@RestController
public class ExtendSmsLoginEndpoint {

    @GetMapping("/login/sms")
    public Map<String, Object> sms(@RequestParam String phone, HttpServletRequest request) {
        String code = SmsVerificationCodeUtils.sendSms(phone);
        request.getSession().setAttribute(ExtendConstant.SMS_ATTRIBUTE_NAME, ExtendUser.PASSWORD_ENCODER.encode(ExtendConstant.SMS_PASSWORD_PREFIX + code));
        //返回下次获取短信的间隔
        Map<String, Object> result = new HashMap<>();
        result.put("status", 0);
        result.put("msg", 60);
        return result;
    }

}

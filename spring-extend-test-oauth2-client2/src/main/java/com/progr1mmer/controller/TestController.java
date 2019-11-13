package com.progr1mmer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Suxy
 * @date 2019/11/4
 * @description file description
 */
@RestController
public class TestController {

    @RequestMapping("/client")
    public String test() {
        return "client2";
    }

    @RequestMapping("/admin/client")
    public String apiTest() {
        return "adminClient2";
    }

}

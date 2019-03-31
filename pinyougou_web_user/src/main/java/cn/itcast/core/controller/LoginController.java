package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public Map<String,String> showName(){
        //从安全框架获取用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        HashMap<String,String> map = new HashMap<>();
        map.put("loginName",name);
        return map;
    }
}

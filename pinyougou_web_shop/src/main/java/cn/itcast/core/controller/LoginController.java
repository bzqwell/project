package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/showName")
    public Map showName(){
        //获取当前登录用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //创建Map存储登录信息
        HashMap<String, Object> map = new HashMap<>();
        //添加数据
        map.put("username",username);
        map.put("curTime",new Date());

        return map;
    }
}

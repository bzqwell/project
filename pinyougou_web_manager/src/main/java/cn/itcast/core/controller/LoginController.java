package cn.itcast.core.controller;

import cn.itcast.core.pojo.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 回显当前登录人
     * @return
     */
    @RequestMapping("/showName")
    public Map showName(HttpServletRequest request){
        //获取Session域中的 securityContext
        SecurityContext securityContext = (SecurityContext) request.getSession().
                getAttribute("SPRING_SECURITY_CONTEXT");

        //获取认证对象
        Authentication authentication = securityContext.getAuthentication();

        //获取用户名
        String username = authentication.getName();

        //封装成Map集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("username",username);
        map.put("curTime",new Date());

        return map;
    }
}

package cn.itcast.core.controller;

import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.service.AddressService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {


    @Reference
    private AddressService addressService;


    /**
     * 查询当前用户地址列表
     * @return
     */
    @RequestMapping("/findListByLoginUser")
    public List<Address> findListByLoginUser(){
        //需要 根据 当前 登录人 查询
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findListByLoginUser(name);
    }
}

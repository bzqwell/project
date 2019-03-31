package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     * 登录认证
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据商家名从数据库查询商家对象
        Seller seller = sellerService.findOne(username);

        //非空判断
        if(seller != null){
            //必须是已审核的商家才能登陆
            if("1".equals(seller.getStatus())){
                //创建该商家的角色列表
                ArrayList<GrantedAuthority> list = new ArrayList<>();
                //添加一个商家角色
                list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
                //返回一个商家详情对象
                return new User(seller.getSellerId(),seller.getPassword(),list);
            }
        }
        //不符合要求,返回null
        return null;
    }
}

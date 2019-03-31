package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.jms.*;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination smsDestination;

    @Autowired
    private UserDao userDao;

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @Override
    public void sendCode(final String phone) {
        //生成验证码
        final String code = RandomStringUtils.randomNumeric(6);
        //将验证码存入redis缓存
        redisTemplate.boundValueOps(phone).set(code);
        //设置有效时间
        //redisTemplate.boundValueOps(phone).expire(1, TimeUnit.HOURS);
        // jms  发消息
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                //添加数据
                mapMessage.setString("phone",phone);
                mapMessage.setString("code",code);
                mapMessage.setString("signName","品优购商城");
                mapMessage.setString("templateCode","SMS_161385235");
                return mapMessage;
            }
        });
    }

    /**
     * 用户注册
     * @param user
     * @param smscode
     */
    @Override
    public void add(User user, String smscode) {
        //从缓存中获取验证码
        String code = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        if(code == null){
            throw new RuntimeException("验证码失效");
        }

        if(code.equals(smscode)){
            //保存用户信息
            user.setCreated(new Date());
            user.setUpdated(new Date());
            //密码加密

            //新增到mysql
            userDao.insertSelective(user);
        }else {
            throw new RuntimeException("验证码错误");
        }
    }
}

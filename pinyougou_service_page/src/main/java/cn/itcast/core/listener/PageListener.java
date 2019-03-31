package cn.itcast.core.listener;

import cn.itcast.core.service.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class PageListener implements MessageListener {

    @Autowired
    private StaticPageService staticPageService;

    @Override
    public void onMessage(Message message) {
        //接收消息
        ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;

        try {
            String id = textMessage.getText();

            // 静态化生成商品详情页
            staticPageService.index(Long.parseLong(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

package cn.itcast.core.listener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemDeleteListener implements MessageListener {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {
        //接收消息
        ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;

        try {
            String id = textMessage.getText();

            // 删除商品 要从索引库把该商品删除   根据 商品id
            SolrDataQuery solrDataQuery = new SimpleQuery(new Criteria("item_goodsid").is(id));
            solrTemplate.delete(solrDataQuery);
            //记得提交
            solrTemplate.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

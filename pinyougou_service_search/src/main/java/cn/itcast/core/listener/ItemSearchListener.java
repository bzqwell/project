package cn.itcast.core.listener;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.List;
import java.util.Map;

public class ItemSearchListener implements MessageListener{

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {
        //接收消息
        ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;

        try {
            String id = textMessage.getText();

            //创建库存量查询对象
            ItemQuery query = new ItemQuery();
            //设置条件:  goods_id  =  该id ; is_default = 1
            query.createCriteria().andGoodsIdEqualTo(Long.parseLong(id)).andIsDefaultEqualTo("1");
            //查询mysql
            List<Item> itemList = itemDao.selectByExample(query);
            //遍历
            for (Item item : itemList) {
                //将规格的 json字符串格式 转为 Map
                String spec = item.getSpec();
                item.setSpecMap(JSON.parseObject(spec, Map.class));
            }
            //添加到索引库,提交
            solrTemplate.saveBeans(itemList,1000);
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}

package vo;

import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;

import java.io.Serializable;
import java.util.List;

public class OrderVo implements Serializable{

    private String nickName;

    private Order order;

    private List<OrderItem> orderItemList;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}

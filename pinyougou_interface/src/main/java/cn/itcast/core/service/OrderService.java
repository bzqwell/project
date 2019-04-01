package cn.itcast.core.service;

import cn.itcast.core.pojo.order.Order;
import entity.PageResult;
import vo.OrderVo;

import java.util.Map;

public interface OrderService {
    void add(Order order);

    PageResult searchOrder(Integer pageNo, Integer rows, OrderVo orderVo);
}

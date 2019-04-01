package cn.itcast.core.controller;

import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.OrderVo;

import java.util.Map;

/**
 * 订单
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    /**
     * 分页条件查询订单(当前用户的)
     * @param pageNo
     * @param rows
     * @param orderVo
     * @return
     */
    @RequestMapping("/searchOrder")
    public PageResult searchOrder(Integer pageNo, Integer rows, @RequestBody OrderVo orderVo){
        //设置当前用户
        orderVo.getOrder().setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        return orderService.searchOrder(pageNo,rows,orderVo);
    }
}

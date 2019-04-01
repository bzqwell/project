package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.order.OrderItemQuery;
import cn.itcast.core.pojo.order.OrderQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.Cart;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;
import vo.OrderVo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderServiceImpl implements OrderService{

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PayLogDao payLogDao;

    @Autowired
    private SellerDao sellerDao;


    /**
     * 新增订单
     * @param order
     */
    @Override
    public void add(Order order) {
        //根据 用户id 查询 该用户的购物车集合(redis中)
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cart").get(order.getUserId());

        //声明变量 累计 支付金额
        double payLogTotalFee = 0;

        //声明集合 保存订单编号
        List orderList = new ArrayList();

        //遍历购物车集合,按商家 分 订单
        for (Cart cart : cartList) {
            //生成分布式自增长id
            order.setOrderId(idWorker.nextId());

            //统计订单编号
            orderList.add(order.getOrderId());

            //支付类型
            order.setPaymentType(order.getPaymentType());
            //支付状态: 未付款
            order.setStatus("1");
            //创建时间
            order.setCreateTime(new Date());
            //修改时间
            order.setUpdateTime(new Date());
            //订单来源
            order.setSourceType("2");
            //商家id
            order.setSellerId(cart.getSellerId());

            //声明一个变量 累计每个购物车的总金额
            double priceTotal  = 0;

            //订单详情表
            for (OrderItem orderItem : cart.getOrderItemList()) {
                //生成分布式自增长id
                orderItem.setId(idWorker.nextId());

                //根据 itemId 从mysql 查询 对应库存量对象
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());

                //商品Id
                orderItem.setGoodsId(item.getGoodsId());
                //订单id
                orderItem.setOrderId(order.getOrderId());
                //标题
                orderItem.setTitle(item.getTitle());
                //单价
                orderItem.setPrice(item.getPrice());
                //小计
                double totalFee = orderItem.getPrice().doubleValue() * orderItem.getNum();
                orderItem.setTotalFee(new BigDecimal(totalFee));
                //图片路径
                orderItem.setPicPath(item.getImage());
                //商家id
                orderItem.setSellerId(item.getSellerId());

                //保存 订单详情表到 mysql
                orderItemDao.insertSelective(orderItem);

                //每保存一个订单详情表,累计一次小计
                priceTotal += totalFee;
            }

            //设置总金额
            order.setPayment(new BigDecimal(priceTotal));

            //累计总支付金额
            payLogTotalFee += order.getPayment().doubleValue();

            //保存 订单表 到 mysql
            orderDao.insertSelective(order);
        }

        //生成支付日志订单,并保存
        PayLog payLog = new PayLog();

        //生成分布式自增长id
        payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
        //订单生成时间
        payLog.setCreateTime(new Date());
        //支付金额
        payLog.setTotalFee((long) payLogTotalFee*100);
        //用户id
        payLog.setUserId(order.getUserId());
        //交易状态
        payLog.setTradeState("0");
        //订单编号列表
        payLog.setOrderList(orderList.toString().replace("[","").replace("]",""));
        //支付类型
        payLog.setPayType("1");

        //保存到 数据库
        payLogDao.insertSelective(payLog);

        //保存缓存一份
        redisTemplate.boundHashOps("payLog").put(payLog.getUserId(),payLog);

        //生成订单购物车要清空
        redisTemplate.boundHashOps("cart").delete(payLog.getUserId());
    }

    /**
     * 分页条件查询订单(当前用户)
     * @param pageNo
     * @param rows
     * @param orderVo
     * @return
     */
    @Override
    public PageResult searchOrder(Integer pageNo, Integer rows, OrderVo orderVo) {
        //创建返回结果集 List<OrderVo>
        List<OrderVo> orderVoList = new ArrayList<>();

        //使用分页助手
        PageHelper.startPage(pageNo,rows);

        //创建订单查询对象
        OrderQuery query = new OrderQuery();

        //设置查询条件
        OrderQuery.Criteria criteria = query.createCriteria();
        // 用户必须是当前用户
        criteria.andUserIdEqualTo(orderVo.getOrder().getUserId());
        // 状态
        if(orderVo.getOrder().getStatus() != null && !"".equals(orderVo.getOrder().getStatus())){
            criteria.andStatusEqualTo(orderVo.getOrder().getStatus());
        }

        //查询order集合
        Page<Order> p = (Page<Order>) orderDao.selectByExample(query);

        //将Order集合对应 装到 OrderVo集合中的
        for (int i = 0; i < p.getResult().size(); i++) {
            orderVoList.add(new OrderVo());
        }

        for (int i = 0; i < p.getResult().size(); i++) {
            orderVoList.get(i).setOrder(p.getResult().get(i));
        }

        //遍历orderVo集合,将每一个vo对象的属性填满
        for (OrderVo vo : orderVoList) {
            //商家店铺名
            vo.setNickName(sellerDao.selectByPrimaryKey(vo.getOrder().getSellerId()).getNickName());
            //订单详情
            OrderItemQuery orderItemQuery = new OrderItemQuery();
            OrderItemQuery.Criteria orderItemCriteria = orderItemQuery.createCriteria();
            orderItemCriteria.andOrderIdEqualTo(vo.getOrder().getOrderId());
            vo.setOrderItemList(orderItemDao.selectByExample(orderItemQuery));
        }

        return new PageResult(p.getTotal(),orderVoList);
    }


}

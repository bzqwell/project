package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartServiceImpl implements CartService{


    @Autowired
    private ItemDao itemDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据 库存量id 查询 对应 库存量对象
     * @param itemId
     * @return
     */
    @Override
    public Item findItemById(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }

    /**
     * 将购物车集合 添加到 redis
     * @param newCartList
     * @param name
     */
    @Override
    public void addCookieToRedis(List<Cart> newCartList, String name) {
        //获取redis 中的 指定 用户 的 购物车数据 (直接转为集合对象)
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("cart").get(name);

        //将cookie中购物车与redis中购物车合并
        oldCartList = mergeCartList(newCartList,oldCartList);

        //将合并后的购物车添加到redis(或替换)
        redisTemplate.boundHashOps("cart").put(name,oldCartList);
    }

    /**
     * 合并购物车
     * @param newCartList
     * @param oldCartList
     * @return
     */
    private List<Cart> mergeCartList(List<Cart> newCartList, List<Cart> oldCartList) {
        if(oldCartList != null && oldCartList.size() > 0){
            if(newCartList != null && newCartList.size() > 0){
                //新、旧购物车都有数据,将新购物车合并到旧购物车中

                //遍历新购物车集合,将每一个商家下的购物车与旧购物车集合比较,看有没看商家相同的
                for (Cart newCart : newCartList) {
                    int index = oldCartList.indexOf(newCart);

                    if(index != -1){
                        // 说明 该新购物车 跟 旧购物车集合的众多商家中 有相同商家
                        //找到旧购物车集合中这个商家的购物车
                        Cart oldCart = oldCartList.get(index);

                        //得到这个商家下的新旧购物车内的商品列表
                        List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();

                        //遍历新购物车的商品列表,看有没有跟旧购物的商品列表中 存在一样的商品
                        for (OrderItem newOrderItem : newOrderItemList) {
                            int index2 = oldOrderItemList.indexOf(newOrderItem);

                            if(index2 != -1){
                                //说明 该新购物车中的商品 跟 旧购物车中的众多商品中 有一样的商品
                                //找到旧购物中这个一样的商品
                                OrderItem oldOrderItem = oldOrderItemList.get(index2);

                                //增加这个商品的数量
                                oldOrderItem.setNum(oldOrderItem.getNum()+newOrderItem.getNum());

                            }else {
                                //说明 该新购物车的商品  跟 旧购物车中的众多商品中 没有一样的商品
                                //直接把该新购物车中的商品,加到旧购物车的商品列表中
                                oldOrderItemList.add(newOrderItem);
                            }
                        }
                    }else {
                        //说明在旧购物集合的众多商家中,没有跟该新购物车的商家一样的
                        //直接把该新购物车加到旧购物车集合中
                        oldCartList.add(newCart);
                    }
                }
                return oldCartList;
            }else {
                //旧购物有数据,新购物车没数据,直接返回 旧购物车
                return oldCartList;
            }
        }else {
            //旧购物车没数据,直接返回 新购物车(此情况下,返回的新购物车可能为null)
            return newCartList;
        }
    }

    /**
     * 从redis中 查询 购物车数据
     * @param name
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        return (List<Cart>) redisTemplate.boundHashOps("cart").get(name);
    }

    /**
     * 填满 购物车数据
     * @param cartList
     * @return
     */
    @Override
    public List<Cart> findCartList(List<Cart> cartList) {
        //遍历购物车集合
        for (Cart cart : cartList) {
            //遍历商品列表
            for (OrderItem orderItem : cart.getOrderItemList()) {
                //根据 库存量id 从mysql 查询 对应 库存量对象
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                //设置商品标题
                orderItem.setTitle(item.getTitle());
                //设置商品图片
                orderItem.setPicPath(item.getImage());
                //设置商品单价
                orderItem.setPrice(item.getPrice());
                //设置商品小计
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //设置店铺名
                cart.setNickName(item.getSeller());
            }
        }
        return cartList;
    }

    /**
     * 替换缓存数据
     * @param cartList
     */
    @Override
    public void updateRedis(List<Cart> cartList,String name) {
        redisTemplate.boundHashOps("cart").put(name,cartList);
    }
}

package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Cart;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("all")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    /**
     *  添加当前商品到购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = {"http://localhost:9003"},allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response){
        try {
            // 1.
            // 声明购物车集合(一个商家算一个购物车,可能包含多个商家)
            List<Cart> cartList = null;

            //获取浏览器所有cookie
            Cookie[] cookies = request.getCookies();

            //非空判断
            if(cookies != null && cookies.length > 0){
                //如果浏览器中已经存在 购物车cookie, 就用 之前声明的集合接收数据
                for (Cookie cookie : cookies) {
                    if("CART".equals(cookie.getName())){
                        cartList = JSON.parseArray(URLDecoder.decode(cookie.getValue(),"utf-8"),Cart.class);
                    }
                }
            }

            // 到这里,如果 购物车集合为null,说明cookie中没有购物车数据,也就是第一次添加商品到购物车;

            //第一次添加,就创建一个购物车集合
            if(cartList == null){
                cartList = new ArrayList<>();
            }

            // 2.
            //不管是不是第一次添加购物车, 先将 购物车数据准备好.  必要数据: 商家id, 库存量id, 商品数量

            Cart newCart = new Cart();
            List<OrderItem> newOrderItemList = new ArrayList<>();
            OrderItem newOrderItem = new OrderItem();

            //根据 参数 库存量id 从mysql 查询 对应的 库存量对象
            Item item = cartService.findItemById(itemId);
            //商家id
            newCart.setSellerId(item.getSellerId());
            //库存量id
            newOrderItem.setItemId(itemId);
            //商品数量
            newOrderItem.setNum(num);

            //因为点击加入购物车,一次只能加一种商品(数量不限),所以这里购物车的商品集合中肯定只有一个商品
            newOrderItemList.add(newOrderItem);
            //将商品列表添加到购物车
            newCart.setOrderItemList(newOrderItemList);

            // 3.
            // 假设不是第一次添加购物车: 判断以前的购物车集合中有没有商家 跟 新建的购物车商家 是一个商家
            // 假设是第一次添加购物车: cartList集合是创建的,自然不会有 跟 新购物车 是一个商家的
            // 所以下面方法都适用
            int index = cartList.indexOf(newCart);

            if(index != -1){
                // 索引不为-1: 说明肯定不是第一次添加购物车,而且旧购物车中有商家 跟 新购物车的商家一样
                // 需要将新商品添加到旧购物车对应的商家内

                //找到一样的商家的旧购物车
                Cart oldCart = cartList.get(index);
                //获得该商家旧购物车内的全部商品列表
                List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();

                //判断旧商品列表中 有没有 跟 新添加的商品 是同一个(有,只需要增加数量;没有,新添加一个商品)
                int index2 = oldOrderItemList.indexOf(newOrderItem);

                if(index2 != -1){
                    // 索引不为 -1: 说明商品列表中 有 商品 跟 新添加的商品 一样 ,只需要增加 数量

                    //找到一样的商品
                    OrderItem orderItem = oldOrderItemList.get(index2);
                    //增加数量
                    orderItem.setNum(orderItem.getNum()+newOrderItem.getNum());
                }else {
                    // 索引为 -1: 说明商品列表中 还没有 新添加的商品 , 直接把新商品 加到 商品列表 就行
                    oldOrderItemList.add(newOrderItem);
                }
            }else {
                // 索引为 -1: 可能是第一次添加,也可能是旧购物车中没有商家 跟 新购物车的商家一样
                // 不管哪种情况,都 只需要 把新购物车添加到 购物车集合 就行
                cartList.add(newCart);
            }


            //到这里, 商品已经添加到购物车集合中

            // 4.
            // 判断登录没有: 登录了,cookie中的购物车数据 要添加到 redis中,并清空购物车cookie; 没登录,更新购物车cookie

            //获取当前用户名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if(!"anonymousUser".equals(name)){
                //不是匿名,说明登录了
                //将购物车集合添加到redis ,需要用到用户名(区分不同用户的redis中购物车数据)
                cartService.addCookieToRedis(cartList,name);

                //添加到redis后就清空cookie, 保护登录用户隐私 (不管原来有没有购物车cookie,用null替换)
                Cookie cookie = new Cookie("CART", null);
                //设置存活时间:立即消失
                cookie.setMaxAge(0);
                //设置全部controller可访问购物车cookie
                cookie.setPath("/");
                //添加或替换
                response.addCookie(cookie);
            }else {
                //是匿名,说明没登录

                //不管原来有没有购物车cookie, 创建新的
                Cookie cookie = new Cookie("CART", URLEncoder.encode(JSON.toJSONString(cartList), "utf-8"));
                //设置存活时间:3天
                cookie.setMaxAge(60*60*24*3);
                //设置全部controller可以访问购物车cookie
                cookie.setPath("/");
                //添加或者替换
                response.addCookie(cookie);
            }

            return new Result(true,"加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"加入购物车失败");
        }
    }


    /**
     * 查询购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){
        // 1.
        //声明购物车集合
        List<Cart> cartList = null;

        //获取所有cookie
        Cookie[] cookies = request.getCookies();

        //非空判断
        if(cookies != null && cookies.length > 0){
            //如果有购物车cookie,就用cartList集合 接收
            for (Cookie cookie : cookies) {
                if("CART".equals(cookie.getName())){
                    try {
                        cartList = JSON.parseArray(URLDecoder.decode(cookie.getValue(),"utf-8"),Cart.class);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //到这里,如果购物车集合为null,说明cookie中没有购物车数据;如果不为Null,说明cookie中有购物车数据

        // 2.
        //判断登陆了没有:登录了,cookie中购物车数据添加到redis中,并清空cookie中购物车数据


        //获取当前用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        if(!"anonymousUser".equals(name)){
            //不为匿名,说明登录了

            if(cartList != null){
                //如果cookie中有购物车数据,先将cookie中购物车数据添加到redis中
                cartService.addCookieToRedis(cartList,name);

                //添加完成后清空购物车cookie,保护用户隐私

                //创建购物车cookie,数据为空
                Cookie cookie = new Cookie("CART", null);
                //设置存活时间:立即失效
                cookie.setMaxAge(0);
                //设置所有controller可访问购物车cookie
                cookie.setPath("/");
                //添加或替换
                response.addCookie(cookie);
            }

            //不管cookie中有没有购物车数据,只要登录了,就从redis中查询
            cartList = cartService.findCartListFromRedis(name);

        }

        // 3.
        // 到这里,如果登录了,cartList中是redis中的数据;如果没登录,cartList中是cookie中的数据
        // 不管cartList中的数据是哪的,只要有数据,就将数据填满(cookie和redis中只存了商家id,库存量id,数量)
        if(cartList != null){
            cartList = cartService.findCartList(cartList);
        }

        return cartList;

    }


    @RequestMapping("/deleteOne")
    public Result deleteOne(String sellerId,Long itemId,HttpServletRequest request,HttpServletResponse response){
        try {
            List<Cart> cartList = null;

            //如果有,获取购物车cookie
            Cookie[] cookies = request.getCookies();
            if(cookies != null && cookies.length > 0){
                for (Cookie cookie : cookies) {
                    if("CART".equals(cookie.getName())){
                        try {
                            cartList = JSON.parseArray(URLDecoder.decode(cookie.getValue(),"utf-8"),Cart.class);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            //判断是否登录: 登录了,修改缓存中数据;未登录,修改cookie中数据
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if(!"anonymousUser".equals(name)){
                //登录了
                //从缓存中获取购物车集合数据
                cartList = cartService.findCartListFromRedis(name);

                //删除指定商家的指定商品
                cartList = deleteOne2(cartList,sellerId,itemId);

                //替换原来的缓存中数据
                cartService.updateRedis(cartList,name);
            }else {
                //未登录
                //删除指定商家的指定商品
                cartList = deleteOne2(cartList,sellerId,itemId);

                //替换原来cookie

                Cookie cookie = new Cookie("CART",URLEncoder.encode(JSON.toJSONString(cartList),"utf-8"));

                cookie.setMaxAge(60*60*24*3);
                cookie.setPath("/");
                response.addCookie(cookie);

            }

            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    /**
     * 删除购物车集合中指定商家下的指定商品
     * @param cartList
     * @param sellerId
     * @param itemId
     * @return
     */
    private List<Cart> deleteOne2(List<Cart> cartList, String sellerId, Long itemId) {
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                List<OrderItem> orderItemList = cart.getOrderItemList();
                //注意索引下标越界异常
                Iterator<OrderItem> iterator = orderItemList.iterator();
                while (iterator.hasNext()){
                    OrderItem orderItem = iterator.next();
                    if(orderItem.getItemId().equals(itemId)){
                        iterator.remove();
                    }
                }
            }
        }
        return cartList;
    }
}

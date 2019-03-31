package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.GoodsVo;

/**
 * 商品管理
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /**
     * 添加商品
     * @param vo
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsVo vo){
        //设置商家id  (利用spring security框架自带的保存登录用户的数据)
        //这里是将商家id保存到数据库
        vo.getGoods().setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());
        try {
            goodsService.add(vo);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }


    /**
     * 根据商品名 条件查询商品
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows,@RequestBody Goods goods){
        //设置当前商家id
        // 这里是给条件查询时的商品对象设置商家id
        goods.setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());
        return goodsService.search(page,rows,goods);
    }


    /**
     * 根据商品id 查询单个商品
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);
    }

    /**
     * 修改商品
     * @param goodsVo
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsVo goodsVo){
        //修改商品不需要设置当前登录人,因为新增的时候已经设置过了
        try {
            goodsService.update(goodsVo);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }


}

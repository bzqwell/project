package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /**
     * 按条件 分页查询
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods){
        // 运营商可以审核所有商家的商品,所以这里不需要设置商家id
        return goodsService.search(page,rows,goods);
    }


    /**
     * 批量审核商品(修改商品状态)
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            goodsService.updateStatus(ids,status);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    /**
     * 批量删除商品(修改商品表的isDelete为1)
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            goodsService.delete(ids);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }
}

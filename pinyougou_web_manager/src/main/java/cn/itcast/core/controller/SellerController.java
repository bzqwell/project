package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家管理
 */
@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference
    private SellerService sellerService;

    /**
     * 商家待审核列表 按条件分页查询
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Seller seller){
        return sellerService.search(page,rows,seller);
    }


    /**
     *  查询单个商家对象
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Seller findOne(String id){
        return sellerService.findOne(id);
    }

    /**
     * 修改商家状态
     * @param sellerId
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(String sellerId,String status){
        try {
            sellerService.updateStatus(sellerId,status);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }
}

package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 品牌管理
 *  @RestController:包含 @Controller 和 @Responsebody 两个注解
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    /**
     * 远程调用
     */
    @Reference
    private BrandService brandService;

    /**
     * 查询所有品牌
     * @return
     */
    @RequestMapping("/findAll")
    public List<Brand> findAll(){
        return brandService.findAll();
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(Integer pageNum,Integer pageSize){
        return brandService.findPage(pageNum,pageSize);
    }

    /**
     * 新增
     * @param brand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand){
        try {
            brandService.add(brand);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }


    /**
     * 查询单个品牌
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Brand findOne(Long id){
        return brandService.findOne(id);
    }

    /**
     * 修改
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete( Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer pageNum,Integer pageSize,
                             @RequestBody Brand brand){
        return brandService.search(pageNum,pageSize,brand);
    }

    /**
     * 模板下拉框查询所有品牌
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}

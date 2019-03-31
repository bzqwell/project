package cn.itcast.core.controller;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.BrandService;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.SpecificationVo;

import java.util.List;
import java.util.Map;

/**
 * 规格管理
 */
@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;

    /**
     * 按条件分页查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page,Integer rows, @RequestBody Specification specification){
        return specificationService.search(page,rows,specification);
    }

    /**
     * 新增规格、规格选项
     * @param specificationVo
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.add(specificationVo);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    /**
     * 查询单个规格,包括规格选项
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public SpecificationVo findOne(Long id){
        return specificationService.findOne(id);
    }

    /**
     * 修改规格、规格选项
     * @param specificationVo
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.update(specificationVo);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    /**
     * 下拉框查询所有规格
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return specificationService.selectOptionList();
    }
}

package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.service.ContentCategoryService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告类型管理
 */
@SuppressWarnings("all")
@RestController
@RequestMapping("/contentCategory")
public class ContentCategoryController {

    @Reference
    private ContentCategoryService contentCategoryService;


    /**
     * 查询所有广告分类
     * @return
     */
    @RequestMapping("/findAll")
    public List<ContentCategory> findAll(){
        return contentCategoryService.findAll();
    }

    /**
     *  按条件 分页查询广告分类列表
     * @param page
     * @param rows
     * @param contentCategory
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody ContentCategory contentCategory){
        return contentCategoryService.search(page,rows,contentCategory);
    }


    /**
     * 新建广告分类
     * @param contentCategory
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody ContentCategory contentCategory){
        try {
            contentCategoryService.add(contentCategory);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    /**
     * 根据id 查询单个广告分类
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public ContentCategory findOne(Long id){
        return contentCategoryService.findOne(id);
    }


    /**
     * 修改广告分类
     * @param id
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ContentCategory contentCategory){
        try {
            contentCategoryService.update(contentCategory);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }


    /**
     * 批量删除广告分类
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            contentCategoryService.delete(ids);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }
}

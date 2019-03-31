package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 广告管理
 */
@SuppressWarnings("all")
@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference
    private ContentService contentService;


    /**
     * 按条件 分页查询
     * @param page
     * @param rows
     * @param content
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Content content){
        return contentService.search(page,rows,content);
    }

    /**
     * 新建 广告
     * @param content
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Content content){
        try {
            contentService.add(content);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }


    /**
     * 查询单个广告对象
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Content findOne(Long id){
        return contentService.findOne(id);
    }


    /**
     * 修改广告
     * @param content
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Content content){
        try {
            contentService.update(content);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    /**
     * 批量删除广告
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            contentService.delete(ids);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }
}

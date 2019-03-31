package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentCategoryDao;
import cn.itcast.core.pojo.ad.ContentCategory;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private ContentCategoryDao contentCategoryDao;

    /**
     * 按条件 分页查询广告分类列表
     * @param page
     * @param rows
     * @param contentCategory
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, ContentCategory contentCategory) {
        //使用分页助手
        PageHelper.startPage(page,rows);
        //没有查询条件
        //查询
        Page<ContentCategory> p = (Page<ContentCategory>) contentCategoryDao.selectByExample(null);
        return new PageResult(p.getTotal(),p.getResult());
    }

    /**
     * 新建广告分类
     * @param contentCategory
     */
    @Override
    public void add(ContentCategory contentCategory) {
        contentCategoryDao.insertSelective(contentCategory);
    }

    /**
     * 根据Id 查询单个广告分类
     * @param id
     * @return
     */
    @Override
    public ContentCategory findOne(Long id) {
        return contentCategoryDao.selectByPrimaryKey(id);
    }

    /**
     * 根据id 修改广告分类
     * @param contentCategory
     */
    @Override
    public void update(ContentCategory contentCategory) {
        contentCategoryDao.updateByPrimaryKeySelective(contentCategory);
    }


    /**
     * 批量删除广告分类
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            contentCategoryDao.deleteByPrimaryKey(id);
        }
    }

    /**
     * 查询所有广告分类
     * @return
     */
    @Override
    public List<ContentCategory> findAll() {
        return contentCategoryDao.selectByExample(null);
    }
}

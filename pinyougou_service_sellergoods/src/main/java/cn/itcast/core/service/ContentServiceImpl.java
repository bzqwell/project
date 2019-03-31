package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 按条件 分页查询
     * @param page
     * @param rows
     * @param content
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Content content) {
        //使用分页助手
        PageHelper.startPage(page,rows);
        //没有查询条件
        //查询
        Page<Content> p = (Page<Content>) contentDao.selectByExample(null);
        return new PageResult(p.getTotal(),p.getResult());
    }


    /**
     * 新建广告
     * @param content
     */
    @Override
    public void add(Content content) {
        // 新增广告要清空一下该广告分类下的所有广告缓存
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        // 添加到mysql
        contentDao.insertSelective(content);
    }

    /**
     * 查询单个广告对象
     * @param id
     * @return
     */
    @Override
    public Content findOne(Long id) {
        return contentDao.selectByPrimaryKey(id);
    }


    /**
     * 修改广告
     * @param content
     */
    @Override
    public void update(Content content) {
        // 修改广告要清空该广告所属分类下所有广告的缓存
        // 查询该广告修改前属于哪个分类
        Long oldCategoryId = contentDao.selectByPrimaryKey(content.getId()).getCategoryId();
        // 判断分类修改了没有,如果没修改,只清空修改后的分类; 如果修改了,以前的分类也要清空
        Long newContendId = content.getCategoryId();
        if(!oldCategoryId.equals(newContendId)){
            //清空修改前的分类缓存
            redisTemplate.boundHashOps("content").delete(oldCategoryId);
        }
        //清空修改后的分类缓存
        redisTemplate.boundHashOps("content").delete(newContendId);
        // 修改mysql
        contentDao.updateByPrimaryKeySelective(content);
    }


    /**
     * 批量删除广告
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length > 0){
            for (Long id : ids) {
                //从mysql查询该id对应的广告
                Content content = contentDao.selectByPrimaryKey(id);
                //清空该广告所属分类下的所有广告的缓存
                redisTemplate.boundHashOps("content").delete(content.getCategoryId());
                // 从mysql删除
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 通过广告分类id 查询广告列表
     * @param categoryId
     * @return
     */
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        // 先从redis缓存中获取,如果没有再去mysql查
        List<Content> list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
        if(list == null || list.size() == 0){
            //创建广告查询对象
            ContentQuery query = new ContentQuery();
            // 设置条件:  根据广告分类id查询,状态必须是启用 "1"
            ContentQuery.Criteria criteria = query.createCriteria();
            criteria.andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
            // 按 sort_order 大小 降序排序
            query.setOrderByClause("sort_order desc");
            //查询
            list = contentDao.selectByExample(query);
            //存入缓存
            redisTemplate.boundHashOps("content").put(categoryId,list);
            //设置缓存有效时间
            redisTemplate.boundHashOps("content").expire(8,TimeUnit.HOURS);
        }

        return list;
    }
}

package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据父id 查询商品分类
     * @param parentId
     * @return
     */
    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        //运营商查询商品分类时,将分类数据存一份到redis缓存
        //查询所有分类
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        //遍历
        for (ItemCat itemCat : itemCatList) {
            //存入缓存  key: 分类名   value: 模板id
            redisTemplate.boundHashOps("itemCatList").put(itemCat.getName(),itemCat.getTypeId());
        }


        //创建商品分类的查询对象
        ItemCatQuery query = new ItemCatQuery();
        //设置查询条件
        ItemCatQuery.Criteria criteria = query.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        //查询
        return itemCatDao.selectByExample(query);
    }

    /**
     * 新建商品分类
     * @param itemCat
     */
    @Override
    public void add(ItemCat itemCat) {
        itemCatDao.insertSelective(itemCat);
    }


    /**
     * 查询单个商品分类
     * @param id
     * @return
     */
    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }

    /**
     * 修改商品分类
     * @param itemCat
     */
    @Override
    public void update(ItemCat itemCat) {
        itemCatDao.updateByPrimaryKeySelective(itemCat);
    }


    /**
     * 批量删除商品分类
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length>0){
            for (Long id : ids) {
                //查询数据库,看该商品分类 有没有子分类
                ItemCat itemCat = itemCatDao.selectByPrimaryKey(id);
                ItemCatQuery query = new ItemCatQuery();
                ItemCatQuery.Criteria criteria = query.createCriteria();
                criteria.andParentIdEqualTo(id);
                List<ItemCat> itemCats = itemCatDao.selectByExample(query);
                if(itemCats == null ||itemCats.size() == 0){
                    //如果没有子分类,就把该商品分类删除
                    itemCatDao.deleteByPrimaryKey(id);
                }else {
                    //如果还有子分类,递归
                    ArrayList<Long> list = new ArrayList<>();
                    for (ItemCat cat : itemCats) {
                        list.add(cat.getId());
                    }
                    Long[] zids = new Long[list.size()];
                    this.delete(list.toArray(zids));
                    //递归完说明没有子分类了,然后把该商品分类删除
                    itemCatDao.deleteByPrimaryKey(id);
                }
            }
        }
    }

    /**
     * 查询所有商品分类
     * @return
     */
    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }
}

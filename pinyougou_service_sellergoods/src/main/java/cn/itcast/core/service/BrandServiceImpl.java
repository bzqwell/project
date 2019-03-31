package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.template.TypeTemplate;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDao brandDao;

    /**
     * 查询所有品牌
     *
     * @return
     */
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }


    /**
     * 分页查询
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        //开启分页助手查询
        PageHelper.startPage(pageNum, pageSize);

        //查询,转为Page类型
        Page<Brand> page = (Page) brandDao.selectByExample(null);

        //创建分页结果对象，并设置总条数、当前页结果集
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新建品牌
     *
     * @param brand
     */
    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }


    /**
     * 查询单个品牌
     *
     * @param id
     * @return
     */
    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }


    /**
     * 修改
     *
     * @param brand
     */
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    /**
     * 删除
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length>0){
            for (Long id : ids) {
                brandDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 条件查询
     *
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {

        //开启分页助手
        PageHelper.startPage(pageNum, pageSize);

        //判断是否有条件需要查询
        BrandQuery brandQuery = new BrandQuery();

        if (brand != null) {

            BrandQuery.Criteria criteria = brandQuery.createCriteria();

            //判断品牌名填了没有
            if (brand.getName() != null && !"".equals(brand.getName().trim())) {
                criteria.andNameLike("%"+brand.getName().trim()+"%");
            }

            //判断品牌首字母填了没有
            if(brand.getFirstChar() != null && !"".equals(brand.getFirstChar().trim())){
                criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
            }
        }

        //根据条件查询
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 模板下拉框查询所有品牌
     * @return
     */
    @Override
    public List<Map> selectOptionList() {
        return brandDao.selectOptionList();
    }


}

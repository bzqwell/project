package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateDao typeTemplateDao;

    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 按条件分页查询
     *
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {

        //运营商查询模板时将 关联品牌、关联规格存入redis缓存
        //查询所有模板数据
        List<TypeTemplate> typeTemplateList = typeTemplateDao.selectByExample(null);

        //遍历
        for (TypeTemplate template : typeTemplateList) {
            //将json格式字符串形式的关联品牌转为List对象
            List<Map> brandList = JSON.parseArray(template.getBrandIds(), Map.class);
            //将关联品牌存入缓存   key: 模板id  value: brandList
            redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);

            //根据模板id查询包含规格选项的规格列表
            List<Map> specList = findBySpecList(template.getId());
            //将关联规格存入缓存   key: 模板id  value: specList
            redisTemplate.boundHashOps("specList").put(template.getId(),specList);
        }



        //使用分页助手
        PageHelper.startPage(page, rows);
        //创建查询对象
        TypeTemplateQuery query = new TypeTemplateQuery();
        //非空判断
        if (typeTemplate != null) {
            TypeTemplateQuery.Criteria criteria = query.createCriteria();

            //判断模板名称填了没有
            if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName().trim())) {
                //设置条件: 模板名模糊查询
                criteria.andNameLike("%" + typeTemplate.getName().trim() + "%");
            }
        }
        //查询,转型
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(query);

        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 新建模板
     *
     * @param typeTemplate
     */
    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    /**
     * 查找单个模板对象
     *
     * @param id
     * @return
     */
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }
        }
    }


    /**
     * 修改模板
     * @param typeTemplate
     */
    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    /**
     * 下拉框查询所有模板
     * @return
     */
    @Override
    public List<Map> selectOptionList() {
        return typeTemplateDao.selectOptionList();
    }


    /**
     * 根据模板id 查询规格列表和规格选项列表
     * @param id
     * @return
     */
    @Override
    public List<Map> findBySpecList(Long id) {
        //根据 模板id 查询单个模板对象
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        //获得模板关联的规格列表
        List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
        //遍历规格列表
        for (Map map : list) {
            //创建规格选项查询对象
            SpecificationOptionQuery query = new SpecificationOptionQuery();
            SpecificationOptionQuery.Criteria criteria = query.createCriteria();
            //查询条件： 根据规格id查询规格选项
            //  注意: Object类型不能直接转Long类型,可以先转为Integer,再转long (转Long也报错)
            criteria.andSpecIdEqualTo((long)(Integer) map.get("id"));
            //查询
            List<SpecificationOption> options = specificationOptionDao.selectByExample(query);
            //添加到map集合
            map.put("options",options);
        }
        return list;
    }
}

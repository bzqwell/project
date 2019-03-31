package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import vo.SpecificationVo;

import javax.management.Query;
import java.util.List;
import java.util.Map;

@Service
@Transactional
//记得加上事务管理
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationDao specificationDao;

    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    /**
     * 按条件分页查询
     *
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        //使用分页助手
        PageHelper.startPage(page, rows);

        //创建查询对象
        SpecificationQuery query = new SpecificationQuery();

        //非空判断
        if (specification != null) {

            SpecificationQuery.Criteria criteria = query.createCriteria();

            //判断有没有查询条件
            if (specification.getSpecName() != null && !"".equals(specification.getSpecName().trim())) {

                //根据specName模糊查询
                criteria.andSpecNameLike("%" + specification.getSpecName().trim() + "%");
            }
        }

        //查询
        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(query);

        return new PageResult(p.getTotal(), p.getResult());
    }


    /**
     * 新增规格、规格选项
     *
     * @param specificationVo
     */
    @Override
    public void add(SpecificationVo specificationVo) {
        //添加规格
        specificationDao.insertSelective(specificationVo.getSpecification());

        //遍历规格选项
        for (SpecificationOption specificationOption : specificationVo.getSpecificationOptionList()) {
            //规格选项的spec_id 为 规格的 id, 需要 getSpecification 主键回显
            specificationOption.setSpecId(specificationVo.getSpecification().getId());
            //添加
            specificationOptionDao.insertSelective(specificationOption);
        }
    }

    /**
     * 查询单个规格,包括规格选项
     *
     * @param id
     * @return
     */
    @Override
    public SpecificationVo findOne(Long id) {
        //创建 SpecificationVo
        SpecificationVo specificationVo = new SpecificationVo();
        //查询规格,并包装
        specificationVo.setSpecification(specificationDao.selectByPrimaryKey(id));

        //创建规格选项的查询对象
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        //查询条件: 规格选项的 specId = 规格 的 id
        SpecificationOptionQuery.Criteria criteria = query.createCriteria();
        criteria.andSpecIdEqualTo(id);
        //查询,并包装
        specificationVo.setSpecificationOptionList(specificationOptionDao.selectByExample(query));

        return specificationVo;
    }


    /**
     * 修改规格、规格选项
     *
     * @param specificationVo
     */
    @Override
    public void update(SpecificationVo specificationVo) {

        //1.修改规格名
        specificationDao.updateByPrimaryKeySelective(specificationVo.getSpecification());

        //2.先清空规格选项

        //创建 规格选项查询对象
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        //设置删除条件:  规格选项的 specID = 规格的 id
        SpecificationOptionQuery.Criteria criteria = query.createCriteria();
        criteria.andSpecIdEqualTo(specificationVo.getSpecification().getId());
        //删除
        specificationOptionDao.deleteByExample(query);

        //3.再重新添加

        //遍历输入的规格选项
        for (SpecificationOption specificationOption : specificationVo.getSpecificationOptionList()) {
            //设置规格选项的 specId
            specificationOption.setSpecId(specificationVo.getSpecification().getId());
            //添加规格选项
            specificationOptionDao.insertSelective(specificationOption);
        }

    }

    //批量删除
    @Override
    public void delete(Long[] ids) {
        //非空判断
        if (ids != null && ids.length > 0) {
            for (Long id : ids) {
                //1. 先删除规格选项

                // 创建规格选项查询对象
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                //设置条件: 规格选项的 specId = 规格的 id
                SpecificationOptionQuery.Criteria criteria = query.createCriteria();
                criteria.andSpecIdEqualTo(id);
                //删除
                specificationOptionDao.deleteByExample(query);

                //2. 再删除规格
                specificationDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 下拉框查询所有规格
     * @return
     */
    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}

package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Brand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /**
     * 查询所有品牌
     * @return
     */
    public List<Brand> findAll();

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult findPage(Integer pageNum, Integer pageSize);

    /**
     * 新建品牌
     * @param brand
     */
    void add(Brand brand);

    /**
     * 查询单个品牌
     * @param id
     * @return
     */
    Brand findOne(Long id);

    /**
     * 修改
     * @param brand
     */
    void update(Brand brand);

    /**
     * 删除
     * @param ids
     */
    void delete(Long[] ids);

    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    PageResult search(Integer pageNum, Integer pageSize, Brand brand);

    List<Map> selectOptionList();
}

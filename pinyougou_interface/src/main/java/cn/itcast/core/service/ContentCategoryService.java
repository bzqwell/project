package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.ContentCategory;
import entity.PageResult;

import java.util.List;

public interface ContentCategoryService {
    PageResult search(Integer page, Integer rows, ContentCategory contentCategory);

    void add(ContentCategory contentCategory);

    ContentCategory findOne(Long id);

    void update(ContentCategory contentCategory);

    void delete(Long[] ids);

    List<ContentCategory> findAll();
}

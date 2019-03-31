package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import entity.PageResult;

import java.util.List;

public interface ContentService {
    PageResult search(Integer page, Integer rows, Content content);

    void add(Content content);

    Content findOne(Long id);

    void update(Content content);

    void delete(Long[] ids);

    List<Content> findByCategoryId(Long categoryId);
}

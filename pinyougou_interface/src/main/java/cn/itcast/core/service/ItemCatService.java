package cn.itcast.core.service;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {
    List<ItemCat> findByParentId(Long parentId);

    void add(ItemCat itemCat);

    ItemCat findOne(Long id);

    void update(ItemCat itemCat);

    void delete(Long[] ids);

    List<ItemCat> findAll();
}

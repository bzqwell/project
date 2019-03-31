package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@SuppressWarnings("all")
@Service
public class ItemsearchServiceImpl implements ItemsearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //创建一个Map集合用于存储要返回的全部数据
        HashMap<String, Object> resultMap = new HashMap<>();

        //搜索前,去除关键字之间的空格
        String keywords = searchMap.get("keywords");
        searchMap.put("keywords",keywords.replaceAll(" ",""));


        //查询商品分类
        List<String> categoryList = findCategoryList(searchMap);
        resultMap.put("categoryList",categoryList);

        //查询 关联品牌、关联规格
        if(categoryList != null && categoryList.size() > 0){
            resultMap.putAll(findBrandListAndSpecListByCategory(categoryList.get(0)));
        }

        //查询 高亮分页结果集
        resultMap.putAll(search2(searchMap));

        return resultMap;
    }

    /**
     * 封装的方法: 根据商品分类名查询关联品牌和关联规格(含规格选项)
     * @param category
     * @return
     */
    private Map<String,Object> findBrandListAndSpecListByCategory(String category){
        //从redis缓存中查询该分类名对应的模板id
        Object specId = redisTemplate.boundHashOps("itemCatList").get(category);

        //从redis缓存中查询该模板id对应的关联品牌
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(specId);

        //从redis缓存中查询该模板id对应的关联规格(含规格选项)
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(specId);

        //创建存放要返回的数据的Map集合
        HashMap<String, Object> resultMap = new HashMap<>();

        //将关联品牌、关联规格存入Map集合
        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);

        return resultMap;
    }

    /**
     * 封装的方法: 查询 高亮分页结果集
     * @param searchMap
     * @return
     */
    private Map<String,Object> search2(Map<String,String> searchMap){
        //创建一个Map集合存储要返回全部数据
        HashMap<String, Object> resultMap = new HashMap<>();

        //创建条件对象: 设置关键字
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

        //创建 高亮查询对象(这里用Query引用没法用高亮的特有方法;创建高亮查询对象自动开启高亮)
        HighlightQuery query = new SimpleHighlightQuery(criteria);

        //创建高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions();

        //设置高亮的域: 标题
        highlightOptions.addField("item_title");

        //设置高亮的内容的前缀 : 红色字体
        highlightOptions.setSimplePrefix("<em style='color:red'>");

        //设置高亮的内容的后缀
        highlightOptions.setSimplePostfix("</em>");

        //设置高亮
        query.setHighlightOptions(highlightOptions);

        // 分页
        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");
        // 设置 偏移量
        query.setOffset((Integer.parseInt(pageNo)-1)*Integer.parseInt(pageSize));
        // 设置 最大显示条数
        query.setRows(Integer.parseInt(pageSize));

        //过滤筛选: 商品分类
        if(searchMap.get("category") != null && !"".equals(searchMap.get("category"))){
            FilterQuery filterQuery = new SimpleFilterQuery(new Criteria("item_category").is(searchMap.get("category")));
            query.addFilterQuery(filterQuery);
        }

        //过滤筛选: 品牌
        if(searchMap.get("brand") != null && !"".equals(searchMap.get("brand"))){
            FilterQuery filterQuery = new SimpleFilterQuery(new Criteria("item_brand").is(searchMap.get("brand")));
            query.addFilterQuery(filterQuery);
        }

        //过滤筛选: 规格
        if(searchMap.get("spec") != null && !"".equals(searchMap.get("spec"))){
            //将spec:{}转为 Map
            Map<String,String> map = JSON.parseObject(searchMap.get("spec"), Map.class);
            //遍历
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                FilterQuery filterQuery = new SimpleFilterQuery(new Criteria("item_spec_"+entry.getKey()).is(entry.getValue()));
                query.addFilterQuery(filterQuery);
            }
        }

        //过滤筛选: 价格
        if(searchMap.get("price") != null && !"".equals(searchMap.get("price"))){
            // 0-500   3000-*
            // 按 - 拆分
            String[] prices = searchMap.get("price").split("-");
            FilterQuery filterQuery = new SimpleFilterQuery();
            //判断有没有*
            if(searchMap.get("price").contains("*")){
                //有*
                filterQuery.addCriteria(new Criteria("item_price").greaterThanEqual(prices[0]));
            }else {
                //没*
                filterQuery.addCriteria(new Criteria("item_price").between(prices[0],prices[1],true,true));
            }
            query.addFilterQuery(filterQuery);
        }

        //排序
        if(searchMap.get("sort") != null && !"".equals(searchMap.get("sort"))){
            if("DESC".equals(searchMap.get("sort"))){
                // 降序
                query.addSort(new Sort(Sort.Direction.DESC,"item_"+searchMap.get("sortField")));
            }else {
                // 升序
                query.addSort(new Sort(Sort.Direction.ASC,"item_"+searchMap.get("sortField")));
            }
        }


        //查询
        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(query, Item.class);

        List<HighlightEntry<Item>> highlighted = page.getHighlighted();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            // entity  , 也就是每个库存量对象
            Item entity = itemHighlightEntry.getEntity();
            // highlights
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
                if(highlights != null && highlights.size() > 0){
                    //如果标题有高亮内容,就将原来的标题替换为高亮显示的标题
                    entity.setTitle(highlights.get(0).getSnipplets().get(0));
                }
        }
        // 存储结果集
        resultMap.put("rows",page.getContent());

        //存储总条数
        resultMap.put("total",page.getTotalElements());

        //存储总页数
        resultMap.put("totalPages",page.getTotalPages());

        return resultMap;
    }

    /**
     * 封装的方法: 查询 普通分页结果集
     * @param searchMap
     * @return
     */
    private Map<String,Object> search1(Map<String,String> searchMap){
        //创建一个Map集合用于存储要返回的全部数据
        HashMap<String, Object> resultMap = new HashMap<>();

        //获取用户输入的关键字
        String keywords = searchMap.get("keywords");

        //创建条件对象 : 在关键字域查询
        Criteria criteria = new Criteria("item_keywords").is(keywords);

        //创建查询对象
        Query query = new SimpleQuery(criteria);

        //获取浏览器传过来的分页属性:当前页
        String pageNo = searchMap.get("pageNo");

        //获取浏览器传过来的分页属性:每页显示的最大条数
        String pageSize = searchMap.get("pageSize");

        //设置分页: 偏移量(从几条数据开始查)
        query.setOffset((Integer.parseInt(pageNo)-1)*Integer.parseInt(pageSize));

        //设置分页: 每页显示的最大条数
        query.setRows(Integer.parseInt(pageSize));

        //分页查询
        ScoredPage<Item> page = solrTemplate.queryForPage(query, Item.class);

        //存储结果集
        resultMap.put("rows",page.getContent());

        //存储总条数
        resultMap.put("total",page.getTotalElements());

        //存储总页数
        resultMap.put("totalPages",page.getTotalPages());

        return resultMap;
    }

    /**
     * 封装的方法: 查询商品分类
     * @param searchMap
     * @return
     */
    private List<String> findCategoryList(Map<String,String> searchMap){

        //创建条件对象 : 关键字 查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

        //创建查询对象
        Query query = new SimpleQuery(criteria);

        //分组 : 按照 商品分类分组
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //创建List存储商品分类列表
        List<String> list = new ArrayList<>();

        //分组查询
        GroupPage<Item> page = solrTemplate.queryForGroupPage(query, Item.class);

        //获取分类信息
        GroupResult<Item> groupResult = page.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        if(content != null && content.size() > 0){
            for (GroupEntry<Item> itemGroupEntry : content) {
                //添加到List 集合
                list.add(itemGroupEntry.getGroupValue());
            }
        }

        return list;
    }
}

package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StaticPageServiceImpl implements StaticPageService,ServletContextAware{

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private GoodsDescDao goodsDescDao;

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private ItemCatDao itemCatDao;

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    //得到该id对应的商品详情页面的路径
    private String getPath(Long id){
        return servletContext.getRealPath(id+".html");
    }

    @Override
    public void index(Long id) {
        //获取freemarker实例
        Configuration configuration = freeMarkerConfigurer.getConfiguration();

        //获得模板生成后的路径
        String path = getPath(id);

        //输出流
        Writer out = null;

        try {
            //创建模板对象 参数为模板文件名
            Template template = configuration.getTemplate("item.ftl");

            //创建输出流 utf-8编码
            out = new OutputStreamWriter(new FileOutputStream(path),"utf-8");

            //数据
            Map<String,Object> root = new HashMap<>();


            //从mysql 标准库存量表 根据 goodsId 查询
            ItemQuery itemQuery = new ItemQuery();
            //条件: 根据 goodsId
            itemQuery.createCriteria().andGoodsIdEqualTo(id);
            //查询
            List<Item> itemList = itemDao.selectByExample(itemQuery);
            //存储
            root.put("itemList",itemList);


            //从mysql 商品详情表 根据 goodsId 查询
            GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
            //存储
            root.put("goodsDesc",goodsDesc);


            //从mysql 商品表 根据 goodsId 查询
            Goods goods = goodsDao.selectByPrimaryKey(id);
            //存储
            root.put("goods",goods);


            //从mysql 商品分类表 根据 分类id 查询
            root.put("itemCat1",itemCatDao.selectByPrimaryKey(goods.getCategory1Id()).getName());
            root.put("itemCat2",itemCatDao.selectByPrimaryKey(goods.getCategory2Id()).getName());
            root.put("itemCat3",itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());

            //处理
            template.process(root,out);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关流
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}

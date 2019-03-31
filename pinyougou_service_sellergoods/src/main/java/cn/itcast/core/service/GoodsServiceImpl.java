package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import vo.GoodsVo;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private GoodsDescDao goodsDescDao;

    @Autowired
    private SellerDao sellerDao;

    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination topicPageAndSolrDestination;

    @Autowired
    private Destination queueSolrDeleteDestination;


    /**
     * 添加商品
     *
     * @param vo
     */
    @Override
    public void add(GoodsVo vo) {
        //设置商品状态为 0 (商品待审核)
        vo.getGoods().setAuditStatus("0");
        //商品表新增 (id要回显,商品详情表id 要和 商品表id 一致)
        goodsDao.insertSelective(vo.getGoods());
        //设置商品详情表id
        vo.getGoodsDesc().setGoodsId(vo.getGoods().getId());
        //商品详情表新增
        goodsDescDao.insertSelective(vo.getGoodsDesc());


        //启用规格才会添加库存量表
        if ("1".equals(vo.getGoods().getIsEnableSpec())) {
            //库存量表新增
            insertItem(vo);
        }


    }

    /**
     * 根据商品名 条件查询商品
     *
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        //使用分页助手
        PageHelper.startPage(page, rows);
        //创建商品条件查询对象
        GoodsQuery query = new GoodsQuery();
        if (goods != null) {
            GoodsQuery.Criteria criteria = query.createCriteria();
            //设置条件: 商品状态 = 选择的状态
            if (goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus())) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            //设置条件: 商品名 模糊查询
            if (goods.getGoodsName() != null && !"".equals(goods.getGoodsName().trim())) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName().trim() + "%");
            }
            //只显示不删除的商品 null是不删除
            criteria.andIsDeleteIsNull();
            // 如果是商家,商品对象中有商家id ; 如果是运营商,商家id 为 null
            if (goods.getSellerId() != null) {
                //商家查询,只能查询自己家的商品
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
        }
        //查询
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(query);

        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 根据商品id 查询单个商品
     *
     * @param id
     * @return
     */
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo = new GoodsVo();
        //查询商品表
        goodsVo.setGoods(goodsDao.selectByPrimaryKey(id));
        //查询商品详情表
        goodsVo.setGoodsDesc(goodsDescDao.selectByPrimaryKey(id));
        //查询库存量表 条件: 库存表的商品id = 商品表的id
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);
        goodsVo.setItemList(itemDao.selectByExample(query));
        return goodsVo;
    }

    /**
     * 修改商品
     *
     * @param goodsVo
     */
    @Override
    public void update(GoodsVo goodsVo) {
        //修改商品表
        goodsDao.updateByPrimaryKeySelective(goodsVo.getGoods());
        //修改商品详情表
        goodsDescDao.updateByPrimaryKeySelective(goodsVo.getGoodsDesc());
        //库存量表,先清空,后新增
        // 1. 清空
        // 创建库存量查询对象
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        // 设置删除条件: 库存量表的 goodsId = 商品表的 id
        criteria.andGoodsIdEqualTo(goodsVo.getGoods().getId());
        // 批量删除
        itemDao.deleteByExample(query);
        // 2. 新增
        // 启用规格才会添加库存量表
        if ("1".equals(goodsVo.getGoods().getIsEnableSpec())) {
            insertItem(goodsVo);
        }
    }

    /**
     * 批量删除商品(修改商品表的isDelete为1)
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        //创建商品对象并设置isDelete为1
        Goods goods = new Goods();
        goods.setIsDelete("1");
        //批量修改
        for (final Long id : ids) {
            goods.setId(id);
            goodsDao.updateByPrimaryKeySelective(goods);

            //发消息 jms
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(id+"");
                }
            });

        }

    }

    /**
     * 批量审核商品(修改商品状态)
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        //创建商品对象并设置状态为传入的数据
        Goods goods = new Goods();
        goods.setAuditStatus(status);
        //批量修改
        for (final Long id : ids) {
            goods.setId(id);
            goodsDao.updateByPrimaryKeySelective(goods);
            //添加到索引库  必须是审核通过 才添加
            if("1".equals(status)){

                //发消息 jms
                jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(id+"");
                    }
                });

            }
        }

    }


    /**
     * 封装的新增库存量表的方法
     *
     * @param vo
     */
    private void insertItem(GoodsVo vo) {
        //库存表新增
        for (Item item : vo.getItemList()) {
            // 1. 标题 :  商品名 + " " + 规格1 + " " + 规格2 + ...
            //先给标题加上商品名
            String title = vo.getGoods().getGoodsName();
            //将提交的json格式字符串转为Map集合
            Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
            //遍历map集合，将每个规格追加在标题上
            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                title += " " + entry.getValue();
            }
            item.setTitle(title);
            // 2. 图片
            // 取出商品详情表中存的图片url的第一条
            List<Map> maps = JSON.parseArray(vo.getGoodsDesc().getItemImages(), Map.class);
            if (null != maps && maps.size() > 0) {
                item.setImage((String) maps.get(0).get("url"));
            }
            // 3. 商品分类id  三级分类的id
            item.setCategoryid(vo.getGoods().getCategory3Id());
            // 4. 创建时间、更新时间
            item.setCreateTime(new Date());
            item.setUpdateTime(new Date());
            // 5.  商品id
            item.setGoodsId(vo.getGoods().getId());
            // 6. 商家id
            item.setSellerId(vo.getGoods().getSellerId());
            // 7. 商家店铺名

            item.setSeller(sellerDao.selectByPrimaryKey(item.getSellerId()).getNickName());
            // 8. 商品分类名
            item.setCategory(itemCatDao.selectByPrimaryKey(item.getCategoryid()).getName());
            // 9. 品牌名
            item.setBrand(brandDao.selectByPrimaryKey(vo.getGoods().getBrandId()).getName());
            //新建一条库存量数据
            itemDao.insertSelective(item);
        }
    }
}

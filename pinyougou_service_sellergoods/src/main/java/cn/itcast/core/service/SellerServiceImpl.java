package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 商家管理
 */
@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerDao sellerDao;

    /**
     * 商家入驻
     *
     * @param seller
     */
    @Override
    public void add(Seller seller) {
        //加密密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        seller.setPassword(passwordEncoder.encode(seller.getPassword()));
        //设置状态
        seller.setStatus("0");
        //保存
        sellerDao.insertSelective(seller);
    }

    /**
     * 商家待审核列表 按条件分页查询
     *
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        //使用分页助手
        PageHelper.startPage(page, rows);
        //创建商家查询对象
        SellerQuery query = new SellerQuery();
        //非空判断
        if (seller != null) {
            SellerQuery.Criteria criteria = query.createCriteria();
            //公司名查询条件
            if (seller.getName() != null && !"".equals(seller.getName().trim())) {
                criteria.andNameLike("%" + seller.getName().trim() + "%");
            }

            //店铺名查询条件
            if (seller.getNickName() != null && !"".equals(seller.getNickName().trim())) {
                criteria.andNickNameLike("%" + seller.getNickName().trim() + "%");
            }

            //商家状态必须是 0
            if (seller.getStatus() != null && !"".equals(seller.getStatus().trim())) {
                criteria.andStatusEqualTo(seller.getStatus().trim());
            }
        }
        //查询
        Page<Seller> p = (Page<Seller>) sellerDao.selectByExample(query);

        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 查询单个商家
     *
     * @param id
     * @return
     */
    @Override
    public Seller findOne(String id) {
        return sellerDao.selectByPrimaryKey(id);
    }

    /**
     * 修改商家状态
     *
     * @param sellerId
     * @param status
     */
    @Override
    public void updateStatus(String sellerId, String status) {
        // 查询该sellerId对应的商家
        Seller seller = sellerDao.selectByPrimaryKey(sellerId);
        // 修改该商家状态
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }


}

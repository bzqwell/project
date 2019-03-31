package cn.itcast.core.service;

import cn.itcast.core.dao.address.AddressDao;
import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.AddressQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressServiceImpl implements AddressService{

    @Autowired
    private AddressDao addressDao;

    /**
     * 查询当前用户 地址列表
     * @param name
     * @return
     */
    @Override
    public List<Address> findListByLoginUser(String name) {
        //创建用户地址信息查询对象
        AddressQuery query = new AddressQuery();
        //设置查询条件: 根据 用户名
        query.createCriteria().andUserIdEqualTo(name);
        return addressDao.selectByExample(query);
    }
}

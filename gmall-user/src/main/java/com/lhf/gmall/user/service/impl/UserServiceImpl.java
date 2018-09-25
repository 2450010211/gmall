package com.lhf.gmall.user.service.impl;


import com.alibaba.fastjson.JSON;
import com.lhf.gmall.bean.UserAddress;
import com.lhf.gmall.bean.UserInfo;
import com.lhf.gmall.service.UserService;
import com.lhf.gmall.user.mapper.UserAddressMapper;
import com.lhf.gmall.user.mapper.UserMapper;
import com.lhf.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserMapper userMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UserInfo> getUsers() {
        return userMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddress() {
        return userAddressMapper.selectAll();
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        UserInfo loginUser = userMapper.selectOne(userInfo);
        if(null != loginUser){
            Jedis jedis = redisUtil.getJedis();
            jedis.setex("user:" + loginUser.getId() + ":info", 60*60*1000*24, JSON.toJSONString(loginUser));
            jedis.close();
        }
        return loginUser;
    }

    @Override
    public List<UserAddress> getUserAddresByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);
        return userAddressList;
    }

    @Override
    public UserAddress getAddressListById(String addressId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(addressId);
        UserAddress shippingAddress = userAddressMapper.selectOne(userAddress);
        return shippingAddress;
    }
}

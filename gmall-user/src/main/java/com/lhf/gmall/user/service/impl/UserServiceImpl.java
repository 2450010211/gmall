package com.lhf.gmall.user.service.impl;


import com.lhf.gmall.bean.UserAddress;
import com.lhf.gmall.bean.UserInfo;
import com.lhf.gmall.service.UserService;
import com.lhf.gmall.user.mapper.UserAddressMapper;
import com.lhf.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> getUsers() {
        return userMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddress() {
        return userAddressMapper.selectAll();
    }
}

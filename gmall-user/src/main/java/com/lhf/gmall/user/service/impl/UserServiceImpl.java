package com.lhf.gmall.user.service.impl;

import com.lhf.gmall.user.bean.UserInfo;
import com.lhf.gmall.user.mapper.UserMapper;
import com.lhf.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-03 0:51
 */

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserMapper userMapper;

    @Override
    public List<UserInfo> getUsers() {
        return userMapper.selectAll();
    }
}

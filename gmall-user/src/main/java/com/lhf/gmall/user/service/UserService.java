package com.lhf.gmall.user.service;

import com.lhf.gmall.user.bean.UserAddress;
import com.lhf.gmall.user.bean.UserInfo;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-03 0:49
 */
public interface UserService {
    /**
     * 查找全部用户
     * @return
     */
    List<UserInfo> getUsers ();

    /**
     * userAddress
     */
    List<UserAddress> getUserAddress();
}

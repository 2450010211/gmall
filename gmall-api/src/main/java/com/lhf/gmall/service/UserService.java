package com.lhf.gmall.service;

import com.lhf.gmall.bean.UserAddress;
import com.lhf.gmall.bean.UserInfo;

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

    UserInfo login(UserInfo userInfo);

    /**
     * 查询用户地址
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddresByUserId(String userId);

    UserAddress getAddressListById(String addressId);
}

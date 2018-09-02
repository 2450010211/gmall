package com.lhf.gmall.user.controller;

import com.lhf.gmall.user.bean.UserAddress;
import com.lhf.gmall.user.bean.UserInfo;
import com.lhf.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-03 0:42
 */
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/user")
//    public List<UserInfo> getUser(){
//        List<UserInfo> users = userService.getUsers();
//        return users;
//    }
    public ResponseEntity<List<UserInfo>> getUser(){
        List<UserInfo> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }

    @RequestMapping(value = "/address")
    public ResponseEntity<List<UserAddress>> getAddress(){
        List<UserAddress> userAddress = userService.getUserAddress();
        return ResponseEntity.ok(userAddress);
    }
}

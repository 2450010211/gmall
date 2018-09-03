package com.lhf.gmall.user.controller;


import com.lhf.gmall.bean.UserAddress;
import com.lhf.gmall.bean.UserInfo;
import com.lhf.gmall.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import java.util.List;

@RestController
public class UserController {

    @Reference
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

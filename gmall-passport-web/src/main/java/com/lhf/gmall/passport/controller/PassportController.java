package com.lhf.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.lhf.gmall.bean.CartInfo;
import com.lhf.gmall.bean.UserInfo;
import com.lhf.gmall.service.CartService;
import com.lhf.gmall.service.UserService;
import com.lhf.gmall.util.CookieUtil;
import com.lhf.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-18 19:50
 */
@Controller
public class PassportController {

    @Reference
    UserService userService;
    @Reference
    CartService cartService;
    /**
     *  跳入登录页面
     * @param returnUrl
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/index")
    public String index(String returnUrl, ModelMap modelMap){
        modelMap.put("originUrl", returnUrl);
        return "index";
    }

    /**
     *  用户登录
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){

        //验证用户名和密码
        UserInfo user = userService.login(userInfo);
        //如果验证成功，根据用户名和密码生成token，然后将该用户信息从db中保存到redis中，设置该用户的过期时间
        //公共部分  私有部分  签名部分
        if(null!= user){
            Map<String,Object> userMap = new HashMap<>();
            userMap.put("userId", user.getId());
            userMap.put("nickName", user.getNickName());
            String ip = getTokenSaleIp(request);
            String token = JwtUtil.encode("gmall0508", userMap, ip);

            //验证成功, 合并购物车
            //1.获取cookie中数据
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            //2.把数据保存合并到db中  将来会通过消息队列的方法来实现
            cartService.combineCart(user.getId(), JSON.parseArray(listCartCookie, CartInfo.class));
            //3.删除cookie中的数据
            CookieUtil.deleteCookie(request, response, "listCartCookie");
            //跳转到拦截器
            return token;
        }else{
            return "err";
        }
    }

    /**
     * 验证用户token
     * @param token
     * @param currentIp
     * @return
     */
    @RequestMapping(value = "/verify")
    @ResponseBody
    public String verify(String token,String currentIp){
        //这个是web系统通过httpClien发送的请求,不是request请求
        //验证token的真伪
        try {
            Map<String, Object> tokenMap = JwtUtil.decode(token, "gmall0508", currentIp);
            //验证token对应的用户信息的过期时间
            if(null != tokenMap){
                //跳转到拦截功能
                return "success";
            }else{
                return "fail";
            }
        } catch (Exception e) {
            return "fail";
        }
    }
    /**
     * 获取客服端Ip
     * @param request
     * @return
     */
    private String getTokenSaleIp(HttpServletRequest request) {
        String ip = "";
        //获得客户端的ip地址
        ip = request.getRemoteAddr();
        if(StringUtils.isBlank(ip)){
            //如果是负载均衡也可以获得到真实的IP
            ip = request.getHeader("x-forwarded-for");
        }else{
            ip = "192.168.234.128";
        }
        return ip;
    }
}

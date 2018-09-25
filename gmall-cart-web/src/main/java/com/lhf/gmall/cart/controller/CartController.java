package com.lhf.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.lhf.gmall.annotation.LoginRequire;
import com.lhf.gmall.bean.CartInfo;
import com.lhf.gmall.bean.SkuInfo;
import com.lhf.gmall.service.CartService;
import com.lhf.gmall.service.SkuService;
import com.lhf.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-15 1:06
 */
@Controller
public class CartController {

    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;

    //动态的加载购物车列表页面
    @LoginRequire(needSuccess = false)
    @RequestMapping(value = "/checkCart")
    public String checkCart(CartInfo cartInfo,ModelMap modelMap,HttpServletRequest request,HttpServletResponse response){
        List<CartInfo> cartInfos = new ArrayList<>();
        String skuId = cartInfo.getSkuId();
        String userId = (String)request.getAttribute("userId");
        if(StringUtils.isNotBlank(userId)){
            //查询db
            cartInfo.setUserId(userId);
            cartService.updateCartByUserId(cartInfo);
            cartInfos = cartService.getCartInfosFromCacheByUserId(userId);
        }else{
            //查询cookie
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartInfos = JSON.parseArray(listCartCookie, CartInfo.class);
            for (CartInfo info : cartInfos) {
                String infoSkuId = info.getSkuId();
                if(info.equals(infoSkuId)){
                    info.setIsChecked(cartInfo.getIsChecked());
                }
            }
            CookieUtil.setCookie(request, response, "listCartCookie", JSON.toJSONString(cartInfos), 1000*60*60*24,true);
        }

        BigDecimal totalPrice = getotalPriceT(cartInfos);
        modelMap.put("totalPrice", totalPrice);
        modelMap.put("cartList", cartInfos);
        return "cartListInner";
    }

    //购物车结算页面
    @LoginRequire(needSuccess = false)
    @RequestMapping(value = "/cartList")
    public String cartList(HttpServletRequest request,ModelMap modelMap){
        List<CartInfo> cartInfos = new ArrayList<>();
        String userId = (String)request.getAttribute("userId");
        if(StringUtils.isBlank(userId)){
            //userId从session中取
            String cookieValue = CookieUtil.getCookieValue(request, "listCartCookie", true);
            if(StringUtils.isNotBlank(cookieValue)){
                cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
            }
        }else{
            //从redis中取
            cartInfos = cartService.getCartInfosFromCacheByUserId(userId);
            //同步缓存
            cartService.flushCartCacheByUserId(userId);
        }
        modelMap.put("cartList", cartInfos);
        BigDecimal totalPrice = getotalPriceT(cartInfos);
        modelMap.put("totalPrice", totalPrice);
        return "cartList";
    }

    private BigDecimal getotalPriceT(List<CartInfo> cartInfos) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfos) {
            String isChecked = cartInfo.getIsChecked();
            if(isChecked.equals("1")){
                totalPrice = totalPrice.add(cartInfo.getCartPrice());
            }
        }
        return totalPrice;
    }

    @LoginRequire(needSuccess = false)
    @RequestMapping(value = "/addToCart")
    public String addCart(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String,String> map){

        //购物车集合对象
        List<CartInfo> cartInfos = new ArrayList<>();

        Integer skuNum = Integer.parseInt(map.get("num"));
        String skuId = map.get("skuId");
        SkuInfo skuInfo = skuService.getSkuById(skuId);
        //填充购物车对象
        CartInfo cartInfo = new CartInfo();
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(skuNum)));
        cartInfo.setSkuNum(skuNum);
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setIsChecked("1");//复选框
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setSkuPrice(skuInfo.getPrice());
        //判断用户是否登录
        String userId = (String)request.getAttribute("userId");

        //未登录
        if(StringUtils.isBlank(userId)){
            //没有用户id
            cartInfo.setUserId("");
            //获取cookie
            String cookieValue = CookieUtil.getCookieValue(request, "listCartCookie", true);
            //判断cookie里有没有数据
            if(StringUtils.isBlank(cookieValue)){
                cartInfos.add(cartInfo);
            }else{
                //cookie里有数据parseArray
                cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
                //判断是不是重复的数据
                boolean isExist = isCartCookie(cartInfos,cartInfo);
                if(isExist){
                    //不存在 新增
                    cartInfos.add(cartInfo);
                }else{
                    //存在 更新
                    for (CartInfo info : cartInfos) {
                        if(info.getSkuId().equals(cartInfo.getSkuId())){
                            info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
                }
            }
            //将购物车数据放入cookie中
            CookieUtil.setCookie(request, response, "listCartCookie", JSON.toJSONString(cartInfos), 1000*60*60*24,true);
        }else{
            //已登录
            cartInfo.setUserId(userId);
            //db中有没有购物车
            CartInfo cartInfoDb = cartService.isCartExist(cartInfo);
            if(null != cartInfoDb){
                //更新
                cartInfoDb.setSkuNum(cartInfoDb.getSkuNum() + cartInfo.getSkuNum());
                cartInfoDb.setCartPrice(cartInfoDb.getCartPrice().multiply(new BigDecimal(cartInfoDb.getSkuNum())));
                cartService.updateCart(cartInfoDb);
            }else{
                //添加
                cartService.insertCart(cartInfo);
            }
            //同步缓存
            cartService.flushCartCacheByUserId(userId);
        }
        return "redirect:/cartSuccess";
    }

    //判断是不是重复的数据
    private boolean isCartCookie(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean flag = true;
        for (CartInfo info : cartInfos) {
            if(info.getSkuId().equals(cartInfo.getSkuId())){
                flag = false;
            }
        }
        return flag;
    }

    @LoginRequire(needSuccess = false)
    @RequestMapping(value = "/cartSuccess")
    public String cartSuccess(){
        return "success";
    }
}

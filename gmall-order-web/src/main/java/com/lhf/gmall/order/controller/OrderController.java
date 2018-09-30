package com.lhf.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.lhf.gmall.annotation.LoginRequire;
import com.lhf.gmall.bean.*;
import com.lhf.gmall.bean.enums.PaymentWay;
import com.lhf.gmall.service.CartService;
import com.lhf.gmall.service.OrderService;
import com.lhf.gmall.service.SkuService;
import com.lhf.gmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author shkstart
 * @create 2018-09-20 19:48
 */

@Controller
public class OrderController {

    @Reference
    UserService userService;
    @Reference
    CartService cartService;
    @Reference
    SkuService skuService;
    @Reference
    OrderService orderService;

    @LoginRequire(needSuccess = true)
    @RequestMapping(value = "/submitOrder")
    public String submitOrder(HttpServletRequest request, String tradeCode, String addressId, ModelMap map) {
        //@LoginRequire(needSuccess = true) 加上这个标签可以直接获取userId
        String userId = (String) request.getAttribute("userId");
        //提交页面之前验证tradeCode是不是回退提交的数据
        boolean tradeCodeExist = orderService.checkTradeCode(userId, tradeCode);

        if (tradeCodeExist) {
            //获取用户的收货地址
            UserAddress userAddress = userService.getAddressListById(addressId);
            List<CartInfo> cartInfos = cartService.getCartInfosFromCacheByUserId(userId);
            //声明订单的对象
            OrderInfo orderInfo = new OrderInfo();
            //流程状态  提交订单	付款成功	商品出库	等待收货	完成
            orderInfo.setProcessStatus("订单提交");
            //订单状态
            orderInfo.setOrderStatus("订单未支付");
            //在当前日期上加一天 订单过期日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            orderInfo.setExpireTime(calendar.getTime());
            //外部订单号  唯一
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = simpleDateFormat.format(new Date());
            String outTradeNo = "atguigugmall" + format + System.currentTimeMillis();
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setConsigneeTel(userAddress.getPhoneNum());
            orderInfo.setCreateTime(new Date());
            orderInfo.setDeliveryAddress(userAddress.getUserAddress());
            orderInfo.setOrderComment("硅谷快递");
            orderInfo.setTotalAmount(getotalPriceT(cartInfos));
            orderInfo.setUserId(userId);
            orderInfo.setPaymentWay(PaymentWay.ONLINE);
            orderInfo.setConsignee(userAddress.getConsignee());
            //订单提交完成之后要删除的购物车的数据
            List<String> cartList = new ArrayList<>();
            List<OrderDetail> orderDetails = new ArrayList<>();

            //获得购物车信息   1.小型网站直接从db中读取数据  2.大型网站一般都在缓存中取数据
            //在缓存中取数据的时候只需要关注价钱和库存不错就行了  其余的信息不是很重要
            //价格是sku中的价格
            for (CartInfo cartInfo : cartInfos) {
                if (cartInfo.getIsChecked().equals("1")) {
                    orderInfo.setImgUrl(cartInfo.getImgUrl());
                    //订单提交完成之后要删除的购物车的数据
                    String cartId = cartInfo.getId();
                    cartList.add(cartId);
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setSkuNum(cartInfo.getSkuNum());
                    orderDetail.setSkuId(cartInfo.getSkuId());
                    orderDetail.setImgUrl(cartInfo.getImgUrl());
                    orderDetail.setSkuName(cartInfo.getSkuName());
                    //检验库存
                    orderDetail.setHasStock("");

                    SkuInfo skuInfo = skuService.getSkuById(cartInfo.getSkuId());
                    //注意//检验价格比较时使用BigDecimal的方法进行比较
                    if (skuInfo.getPrice().compareTo(cartInfo.getSkuPrice()) == 0) {
                        orderDetail.setOrderPrice(cartInfo.getCartPrice());
                    } else {
                        return "ordererr";
                    }
                    orderDetails.add(orderDetail);
                }
            }
            orderInfo.setOrderDetailList(orderDetails);
            //保存订单
            orderService.saveOrder(orderInfo);
            //删除购物车中的数据
            cartService.deleteCart(StringUtils.join(cartList, ","), userId);
            //重定向到支付系统
            return "redirect:http://localhost:8087/index?outTradeNo=" + outTradeNo + "&totalAmount=" + getotalPriceT(cartInfos);
        } else {
            return "ordererr";
        }
    }

    //模拟添加购物车登陆 只是显示数据不保存数据
    @LoginRequire(needSuccess = true)
    @RequestMapping(value = "/toTrade")
    public String toTrade(HttpServletRequest request, ModelMap modelMap) {
        //在拦截器中已经把userId注入进去却拿不到userId的原因是
        //springboot主函数没有和拦截器在同一目录下
        String userId = (String) request.getAttribute("userId");
        //获取用户收货地址
        List<UserAddress> userAddresses = userService.getUserAddresByUserId(userId);
        //获取用户订单详情列表
        List<OrderDetail> orderDetails = new ArrayList<>();
        List<CartInfo> cartInfosFromCacheByUserId = cartService.getCartInfosFromCacheByUserId(userId);

        for (CartInfo cartInfo : cartInfosFromCacheByUserId) {
            if (cartInfo.getIsChecked().equals("1")) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setOrderPrice(new BigDecimal("0"));
                orderDetails.add(orderDetail);
            }
        }
        BigDecimal totalAmount = getotalPriceT(cartInfosFromCacheByUserId);
        modelMap.put("orderDetailList", orderDetails);
        modelMap.put("userAddressList", userAddresses);
        modelMap.put("totalAmount", totalAmount);
        //提交订单回退的解决方式
        //生成一个唯一的交易码
        String tradeCode = orderService.getTradeCode(userId);
        modelMap.put("tradeCode", tradeCode);
        return "trade";
    }

    private BigDecimal getotalPriceT(List<CartInfo> cartInfos) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfos) {
            String isChecked = cartInfo.getIsChecked();
            if (isChecked.equals("1")) {
                totalPrice = totalPrice.add(cartInfo.getCartPrice());
            }
        }
        return totalPrice;
    }

    /**
     * 拆单
     * @param request
     * @return
     */
    @RequestMapping(value = "orderSplit",method = RequestMethod.POST)
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMapJson = request.getParameter("wareSkuMap");
        List<OrderInfo> subOrderInfoList = orderService.splitOrder(orderId,wareSkuMapJson);

        List wareSkuMapList=new ArrayList();
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            wareSkuMapList.add(map);
        }
        String newWareSkuMapJson = JSON.toJSONString(wareSkuMapList);
        return newWareSkuMapJson;
    }


}

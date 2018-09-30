package com.lhf.gmall.service;

import com.lhf.gmall.bean.OrderInfo;

import java.util.List;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-21 1:29
 */
public interface OrderService {

    void saveOrder(OrderInfo orderInfo);

    String getTradeCode(String userId);

    boolean checkTradeCode(String userId, String tradeCode);

    OrderInfo getOrderInfoByTradeNo(String outTradeNo);

    OrderInfo updateOrder(OrderInfo orderInfo);

    void sendOrderResultQueue(OrderInfo orderInfo);

    List<OrderInfo> splitOrder(String orderId, String wareSkuMapJson);

    Map initWareOrder(OrderInfo orderInfo);
}

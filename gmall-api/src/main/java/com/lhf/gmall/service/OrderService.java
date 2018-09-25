package com.lhf.gmall.service;

import com.lhf.gmall.bean.OrderInfo; /**
 * @author shkstart
 * @create 2018-09-21 1:29
 */
public interface OrderService {

    void saveOrder(OrderInfo orderInfo);

    String getTradeCode(String userId);

    boolean checkTradeCode(String userId, String tradeCode);

    OrderInfo getOrderInfoByTradeNo(String outTradeNo);
}

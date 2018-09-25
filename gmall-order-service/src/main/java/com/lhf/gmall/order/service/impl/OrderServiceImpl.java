package com.lhf.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lhf.gmall.bean.OrderDetail;
import com.lhf.gmall.bean.OrderInfo;
import com.lhf.gmall.order.mapper.OrderDetailMapper;
import com.lhf.gmall.order.mapper.OrderInfoMapper;
import com.lhf.gmall.service.OrderService;
import com.lhf.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

/**
 * @author shkstart
 * @create 2018-09-21 12:07
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    RedisUtil redisUtil;

    /**
     * 提交订单之后保存orderDetail和orderInfo
     * @param orderInfo
     */
    @Override
    public void saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);
        String orderId = orderInfo.getId();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insertSelective(orderDetail);
        }
    }

    @Override
    public String getTradeCode(String userId) {
        String uuid = UUID.randomUUID().toString();
        //将tradeCode保存到redis中
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:" + userId + ":tradeCode", 1000*60*15, uuid);

        jedis.close();
        return uuid;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {
        boolean flag = false;
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeRedis = jedis.get("user:" + userId + ":tradeCode");
        if(null != tradeCodeRedis && tradeCodeRedis.equals(tradeCode)){
            flag = true;
            jedis.del("user:" + userId + ":tradeCode");
        }
        jedis.close();
        return flag;
    }

    @Override
    public OrderInfo getOrderInfoByTradeNo(String outTradeNo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        OrderInfo orderInfoPayment = orderInfoMapper.selectOne(orderInfo);
        String orderId = orderInfoPayment.getId();

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);

        orderInfoPayment.setOrderDetailList(orderDetails);
        return orderInfoPayment;
    }
}

package com.lhf.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.lhf.gmall.bean.OrderDetail;
import com.lhf.gmall.bean.OrderInfo;
import com.lhf.gmall.bean.enums.ProcessStatus;
import com.lhf.gmall.order.mapper.OrderDetailMapper;
import com.lhf.gmall.order.mapper.OrderInfoMapper;
import com.lhf.gmall.service.OrderService;
import com.lhf.gmall.util.ActiveMQUtil;
import com.lhf.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
    @Autowired
    ActiveMQUtil activeMQUtil;

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

    @Override
    public OrderInfo updateOrder(OrderInfo orderInfo) {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", orderInfo.getOutTradeNo());
        orderInfoMapper.updateByExampleSelective(orderInfo, example);

        orderInfo = orderInfoMapper.selectOne(orderInfo);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);

        return orderInfo;
    }

    @Override
    public void sendOrderResultQueue(OrderInfo orderInfo) {
        //库存的结果的消息队列
        Connection connection = activeMQUtil.getConnection();
        Session session = null;
        try {
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(order_result_queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            TextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(JSON.toJSONString(orderInfo));
            producer.send(textMessage);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            if(session != null){
                try {
                    session.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            if(connection != null){
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }












    /**
     * 拆单
     * @param orderId
     * @param wareSkuMapJson
     * @return
     */
    @Override
    public  List<OrderInfo>  splitOrder(String orderId,String wareSkuMapJson){
        List<OrderInfo> subOrderList=new ArrayList<>();
        //转List  循环list获得 仓库与sku的对应关系
        List<Map> wareSkuMapList = JSON.parseArray(wareSkuMapJson, Map.class);
        OrderInfo orderInfo = getOrderInfo(orderId);
        for (Map map : wareSkuMapList) {
            String wareId = (String)map.get("wareId");
            List<String> skuidList = (List)map.get("skuIds");

            //每个仓库拆分成一个子订单
            //构造主表
            OrderInfo subOrderInfo=new OrderInfo();
            try {
                BeanUtils.copyProperties(subOrderInfo,orderInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            subOrderInfo.setParentOrderId(orderInfo.getId());
            subOrderInfo.setId(null);
            subOrderInfo.setWareId(wareId);
            List<OrderDetail> subOrderDetailList=new ArrayList<>();
            for (String skuId : skuidList) {
                for (OrderDetail orderDetail :orderInfo.getOrderDetailList()) {
                    if(skuId.equals(orderDetail.getSkuId())){
                        orderDetail.setOrderId(null);
                        orderDetail.setId(null);
                        subOrderDetailList.add(orderDetail);
                    }
                }
            }
            subOrderInfo.setOrderDetailList(subOrderDetailList);

            subOrderInfo.sumTotalAmount();

            saveOrder(subOrderInfo);

            updateOrderStatus(orderInfo.getId(), ProcessStatus.SPLIT);

            subOrderList.add(subOrderInfo);
        }

        return subOrderList;
    }

    private void updateOrderStatus(String id, ProcessStatus split) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(id);
        orderInfo.setProcessStatus(split.toString());
       orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    private OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        return orderInfoMapper.selectOne(orderInfo);
    }

    @Override
    public Map  initWareOrder(OrderInfo orderInfo){

        Map map=new HashMap();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getTradeBody());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId());

        List detailList=new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap =new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());

            detailList.add(detailMap);
        }

        map.put("details",detailList);

        return  map;

    }

}

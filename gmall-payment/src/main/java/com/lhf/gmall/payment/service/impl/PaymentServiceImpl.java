package com.lhf.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.lhf.gmall.bean.PaymentInfo;
import com.lhf.gmall.payment.mapper.PaymentInfoMapper;
import com.lhf.gmall.service.PaymentService;
import com.lhf.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-23 17:39
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", paymentInfo.getOutTradeNo());
        paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
    }

    @Override
    public void sendPaymentSuccessQueue(String outTradeNo, String tradeNo) {
        Connection connection = activeMQUtil.getConnection();
        Session session = null;
        try {
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo", outTradeNo);
            mapMessage.setString("tradeNo", tradeNo);
            producer.send(mapMessage);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendDelayPaymentResult(String outTradeNo, int i) {
        //创建延迟队列
        System.out.println("开始发送延迟检查队列支付状态的队列");
        Connection connection = activeMQUtil.getConnection();
        Session session = null;
        try {
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue payment_check_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_check_queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo", outTradeNo);
            mapMessage.setInt("count", i);
            //创建延迟时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 15);
            producer.send(mapMessage);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Map<String,String> checkAlipayPayment(String outTradeNo) {

        Map<String, String> returnMap = new HashMap<>();
        System.out.println("开始检查支付宝支付的状态");

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, String> map = new HashMap<>();
        //根据外部订单号去支付宝查询支付状态  out_trade_no
        map.put("out_trade_no", outTradeNo);
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            //获取支付状态
            String tradeStatus = response.getTradeStatus();
            String tradeNo = response.getTradeNo();
            String msg = response.getMsg();
            if(StringUtils.isNotBlank(tradeStatus)){
                returnMap.put("status", tradeStatus);
                returnMap.put("tradeNo", tradeNo);
                returnMap.put("msg", msg);
                return returnMap;
            }else{
                returnMap.put("status", "fail");
                return returnMap;
            }
        } else {
            System.out.println("用户未创建交易");
            returnMap.put("status", "fail");
            return returnMap;
        }
    }

    @Override
    public boolean checkStatus(String outTradeNo) {
        boolean flag = false;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo = paymentInfoMapper.selectOne(paymentInfo);
        if(paymentInfo.getPaymentStatus().equals("已支付")){
            flag = true;
        }
        return flag;
    }

    @Override
    public void updatePaymentSuccess(PaymentInfo paymentInfo) {
        //更新支付信息
        updatePayment(paymentInfo);
        //通知订单系统,更新订单信息,创建消息队列
        String outTradeNo = paymentInfo.getOutTradeNo();
        sendPaymentSuccessQueue(outTradeNo,paymentInfo.getAlipayTradeNo());
    }
}

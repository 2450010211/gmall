package com.lhf.gmall.order.orderMq;

import com.lhf.gmall.bean.OrderInfo;
import com.lhf.gmall.bean.enums.PaymentWay;
import com.lhf.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @author shkstart
 * @create 2018-09-25 20:50
 */
@Component
public class OrderPaymentSuccessQueueListener {

    @Autowired
    OrderService orderService;

    @JmsListener(destination ="PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        String outTradeNo = mapMessage.getString("outTradeNo");
        String trackingNo = mapMessage.getString("tradeNo");
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setOrderStatus("订单已支付");
        orderInfo.setPaymentWay(PaymentWay.ONLINE);
        orderInfo.setTrackingNo(trackingNo);
        orderInfo.setProcessStatus("订单已支付");
        OrderInfo orderInfoQueue = orderService.updateOrder(orderInfo);

        //发送订单状态通知,通知库存
        orderService.sendOrderResultQueue(orderInfoQueue);
    }
}

package com.lhf.gmall.payment.paymentMq;

import com.lhf.gmall.bean.PaymentInfo;
import com.lhf.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-26 19:02
 */
@Component
public class PaymentCheckQueueListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeCheckResult(MapMessage mapMessage) throws JMSException {
        int count = mapMessage.getInt("count");
        String outTradeNo = mapMessage.getString("outTradeNo");
        //调用支付宝接口,检查支付状态
        Map<String, String> tradeStatus = paymentService.checkAlipayPayment(outTradeNo);
        //根据支付的状态是否调用延迟队列
        if (tradeStatus.get("status").equals("TRADE_SUCCESS")) {
            //支付状态的幂等性判断
            boolean flag = paymentService.checkStatus(outTradeNo);
            if (!flag) {
                //交易成功 更新支付信息
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setAlipayTradeNo(tradeStatus.get("tradeNo"));
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent(tradeStatus.get("msg"));
                paymentInfo.setOutTradeNo(outTradeNo);
                //修改支付信息
                System.err.println("订单已支付");
                paymentService.updatePaymentSuccess(paymentInfo);
            } else {
                System.err.println("检查到该订单已支付完毕,消息队列结束");
            }
        } else {
            if (count > 0) {
                System.err.println("进行第" + (6 - count) + "次检查用户支付的状态,未支付,继续发送延迟队列");
                paymentService.sendDelayPaymentResult(outTradeNo, count - 1);
            } else {
                System.err.println("检查次数上限,用户在规定时间内没有支付");
            }
        }
    }
}

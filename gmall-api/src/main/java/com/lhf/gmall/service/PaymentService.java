package com.lhf.gmall.service;

import com.lhf.gmall.bean.PaymentInfo;

import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-23 17:38
 */
public interface PaymentService {

    void savePayment(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendPaymentSuccessQueue(String outTradeNo,String tradeNo);

    void sendDelayPaymentResult(String outTradeNo, int i);

    Map<String,String> checkAlipayPayment(String outTradeNo);

    boolean checkStatus(String outTradeNo);

    void updatePaymentSuccess(PaymentInfo paymentInfo);
}

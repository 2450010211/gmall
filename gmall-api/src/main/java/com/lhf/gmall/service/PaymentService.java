package com.lhf.gmall.service;

import com.lhf.gmall.bean.PaymentInfo; /**
 * @author shkstart
 * @create 2018-09-23 17:38
 */
public interface PaymentService {

    void savePayment(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);
}

package com.lhf.gmall.payment.service.impl;

import com.lhf.gmall.bean.PaymentInfo;
import com.lhf.gmall.payment.mapper.PaymentInfoMapper;
import com.lhf.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

/**
 * @author shkstart
 * @create 2018-09-23 17:39
 */
@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

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
}

package com.lhf.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.lhf.gmall.bean.OrderInfo;
import com.lhf.gmall.bean.PaymentInfo;
import com.lhf.gmall.payment.conf.AlipayConfig;
import com.lhf.gmall.service.OrderService;
import com.lhf.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-23 2:06
 */
@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;
    @Reference
    OrderService orderService;
    @Autowired
    PaymentService paymentService;


    /**
     * 支付完成回调的方法
     * @param request
     * @return
     */
    @RequestMapping(value = "/alipay/callback/return")
    public String alipayCallbackReturn(HttpServletRequest request) {
        /**
         * http://localhost:8087/alipay/callback/return?
         * charset=utf-8
         * &out_trade_no=atguigugmall201809231801271537696887684
         * &method=alipay.trade.page.pay.return
         * &total_amount=0.01
         * sign签名
         * &sign=ePPrxpPXbocc41AcmDDVUyY%2B%2FzxNm4FG%2B1lHSWLxmO6Jyf3RrLEq2JExjeOfxQxxPNgJM%2BZjIVkmxZNEkp%2Bj%2FjIet2XZ6i2u0rgW6nosIVFQCRy8hZ%2FqYEFxDraapBmlKjGNbm9qhbD0m6b0%2F%2Bl3PURxk4yLvIdM2OQwbbjWykmwx1mrLZ7LXeeHItfvC1xSgm6AOfujjxEkCn9XLevUKSf%2BkyA%2BLZdT%2BrE72faJt4Znixro9gPz5%2FspUlLRBSqAXAiZrtNzEqlXa9xuZ79jkbNzC6%2FBWzG2F2ZbQtoA86olbuBT2cEgbk5jb2JRUJrLN8F7KRYI0ScveBgF6pQ%2Bhg%3D%3D
         * &trade_no=2018092322001465860598254367
         * &auth_app_id=2018020102122556
         * &version=1.0
         * &app_id=2018020102122556
         * &sign_type=RSA2
         * &seller_id=2088921750292524
         * &timestamp=2018-09-23+18%3A03%3A05
         */
        //验证发送的请求者是不是支付宝
        try {
            boolean flag = AlipaySignature.rsaCheckV1(null, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
            if (flag) {
                //验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            } else {
                // TODO 验签失败则记录异常日志，并在response中返回failure.
            }
        } catch (Exception e) {
            //只有在异步的时候才有效
            System.out.println("验证阿里的签名");
        }

        //如果是异步通知提交可以直接使用从paramsMap中拿数据
        String tradeNo = request.getParameter("trade_no");
        //获取查询的字符串  只对get方法有效
        String callback = request.getQueryString();
        String outTradeNo = request.getParameter("out_trade_no");
        //更新支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAlipayTradeNo(tradeNo);
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(callback);
        paymentInfo.setOutTradeNo(outTradeNo);

        paymentService.updatePaymentSuccess(paymentInfo);
        return "finish";
    }

    /**
     * 选择支付方式
     * @param outTradeNo
     * @return
     */
    @RequestMapping(value = "/alipay/submit")
    @ResponseBody
    public String alipaySubmit(String outTradeNo) {

        OrderInfo orderInfo = orderService.getOrderInfoByTradeNo(outTradeNo);

        // 设置支付宝page.pay的请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", outTradeNo);
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", 0.01);
        map.put("subject", orderInfo.getOrderDetailList().get(0).getSkuName());
        map.put("body", "硅谷支付产品测试");
        String s = JSON.toJSONString(map);
        alipayRequest.setBizContent(s);//填充业务参数

        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        // 保存交易信息
        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setOrderId(orderInfo.getId());

        paymentService.savePayment(paymentInfo);

        //通知延迟任务开始执行
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),5);
        return form;
    }

    /**
     * 选择支付方式页面
     *
     * @param request
     * @param outTradeNo
     * @param totalAmount
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request, String outTradeNo, String totalAmount, ModelMap modelMap) {
        //支付页面选择需要传递二个参数
        modelMap.put("outTradeNo", outTradeNo);
        modelMap.put("totalAmount", totalAmount);
        return "index";
    }
}

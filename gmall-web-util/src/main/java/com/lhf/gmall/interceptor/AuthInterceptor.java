package com.lhf.gmall.interceptor;

import com.lhf.gmall.annotation.LoginRequire;
import com.lhf.gmall.util.CookieUtil;
import com.lhf.gmall.util.HttpClientUtil;
import com.lhf.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-18 20:33
 */
@Component //相当于在spring中配置<bean class=">标签
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(null == methodAnnotation){
            //放行
            return true;
        }else{
            //因为添加购物车有两种结果  获取注解上面的值 true：需要登录
            boolean needSuccess = methodAnnotation.needSuccess();
            String token = "";

            //从cookie中获取token,说明用户登录过一次
            String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
            //从浏览器地址栏中获取token,说明该用户第一次登录
            String newToken = request.getParameter("token");

            //oldToken空，newToken空 从未登陆
            //oldToken空，newToken不空 第一次登录
            if(StringUtils.isBlank(oldToken) && StringUtils.isNotBlank(newToken)){
                token = newToken;
            }
            //oldToken不空，newToken空 之前登录过
            if(StringUtils.isNotBlank(oldToken) && StringUtils.isBlank(newToken)){
                token = oldToken;
            }
            //oldToken不空，newToken不空 说明cookie中的token过期了
            if(StringUtils.isNotBlank(oldToken) && StringUtils.isNotBlank(newToken)){
                token = newToken;
            }
            //login登录页面验证
            if(StringUtils.isNotBlank(token)){
               //进行验证
                String ip = getTokenSaleIp(request);
                String url = "http://localhost:8085/verify?token=" + token + "&currentIp=" + ip;
                String success = HttpClientUtil.doGet(url);
                //用户验证token完成
                if(success.equals("success")){
                    //程序走到这里说明 1.用户需要进行验证的 2.访问过程中携带token的 3.这个token是正确的
                    //将cookie重新写入浏览器  设置token的过期时间
                    CookieUtil.setCookie(request, response, "oldToken", token, 1000*60*60*24, true);
                    //将用户信息放入请求中

                    Map<String, Object> tokenMap = JwtUtil.decode(token,"gmall0508", ip);
                    request.setAttribute("userId", tokenMap.get("userId"));
                    request.setAttribute("nickName", tokenMap.get("nickName"));
                    return true;
                }
            }
            //
            if(needSuccess){
                //登录之后重新跳转到结算页面 需要在当前地址后面加上之前的地址
                response.sendRedirect("http://localhost:8085/index?returnUrl=" + request.getRequestURL());
                return false;
            }else{
                return true;
            }
        }
    }

    /**
     * 获取客服端Ip
     * @param request
     * @return
     */
    private String getTokenSaleIp(HttpServletRequest request) {
        String ip = "";
        //获得客户端的ip地址
        ip = request.getRemoteAddr();
        if(StringUtils.isBlank(ip)){
            //如果是负载均衡也可以获得到真实的IP
            ip = request.getHeader("x-forwarded-for");
        }else{
            ip = "192.168.234.128";
        }
        return ip;
    }
}

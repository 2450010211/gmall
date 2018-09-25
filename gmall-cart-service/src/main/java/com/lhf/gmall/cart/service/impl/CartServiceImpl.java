package com.lhf.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.lhf.gmall.bean.CartInfo;
import com.lhf.gmall.cart.mapper.CartInfoMapper;
import com.lhf.gmall.service.CartService;
import com.lhf.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-15 17:08
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo isCartExist(CartInfo cartInfo) {

        Example example = new Example(CartInfo.class);
        String userId = cartInfo.getUserId();
        String skuId = cartInfo.getSkuId();
        example.createCriteria().andEqualTo("userId", userId).andEqualTo("skuId", skuId);
        CartInfo cartInfoReturn = cartInfoMapper.selectOneByExample(example);
        return cartInfoReturn;
    }

    @Override
    public void updateCart(CartInfo cartInfoDb) {
        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDb);
        getCartInfosFromCacheByUserId(cartInfoDb.getUserId());
    }

    @Override
    public void insertCart(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
        getCartInfosFromCacheByUserId(cartInfo.getUserId());
    }

    @Override
    public void flushCartCacheByUserId(String userId) {
        //刷新缓存
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);
        if (null != cartInfos && cartInfos.size() > 0) {
            //为了提高性能 我们存储数据的时候使用redis的数据格式hash来存储
            Map<String, String> cartInfoParam = new HashMap<>();
            for (CartInfo info : cartInfos) {
                cartInfoParam.put(info.getId(), JSON.toJSONString(info));
            }
            //把数据保存到redis中
            Jedis jedis = redisUtil.getJedis();
            // 将购物车的hashMap放入redis
            jedis.del("cart:" + userId + ":info");
            //更新之前要先删除redis中的数据
            jedis.hmset("cart:" + userId + ":info", cartInfoParam);
            jedis.close();
        } else {
            //清理redis
            Jedis jedis = redisUtil.getJedis();
            jedis.del("cart:" + userId + ":info");
            jedis.close();
        }
    }

    @Override
    public List<CartInfo> getCartInfosFromCacheByUserId(String userId) {
        //从redis中取userId
        List<CartInfo> cartInfos = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> cartInfoList = jedis.hvals("cart:" + userId + ":info");
        if (cartInfoList != null && cartInfoList.size() > 0) {
            for (String cartInfoJson : cartInfoList) {
                CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
                cartInfos.add(cartInfo);
            }
        }
        jedis.close();
        return cartInfos;
    }

    @Override
    public void updateCartByUserId(CartInfo cartInfo) {
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", cartInfo.getUserId())
                .andEqualTo("skuId", cartInfo.getSkuId());
        cartInfoMapper.updateByExampleSelective(cartInfo, example);
        //同步缓存
        flushCartCacheByUserId(cartInfo.getUserId());
    }

    @Override
    public void combineCart(String userId, List<CartInfo> cartInfoCookie) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfoDb = cartInfoMapper.select(cartInfo);
        //合并购物车中的数据
        //cookie中的数据
        if (cartInfoCookie != null && cartInfoCookie.size() > 0) {
            for (CartInfo cartCookie : cartInfoCookie) {
                //根据skuId进行合并 因为cookie中的数据没有userId
                boolean flag = true;
                if (cartInfoDb != null && cartInfoDb.size() > 0) {
                    flag = isCartCookie(cartInfoDb, cartCookie);
                }
                if (!flag) {
                    CartInfo cartDb = new CartInfo();
                    for (CartInfo info : cartInfoDb) {
                        if (info.getSkuId().equals(cartCookie.getSkuId())) {
                            cartDb = info;
                        }
                    }
                    //更新
                    cartDb.setSkuNum(cartCookie.getSkuNum());
                    cartDb.setIsChecked(cartCookie.getIsChecked());
                    cartDb.setCartPrice(cartDb.getSkuPrice().multiply(new BigDecimal(cartDb.getSkuNum())));
                    //更新数据库
                    cartInfoMapper.updateByPrimaryKeySelective(cartDb);
                } else {
                    //添加
                    cartCookie.setUserId(userId);
                    cartInfoMapper.insertSelective(cartCookie);
                }
            }
        }
        //同步缓存
        flushCartCacheByUserId(userId);
    }

    /**
     * 批量删除购物车已经结算的商品
     *
     * @param join
     */
    @Override
    public void deleteCart(String join,String userId) {
        if(StringUtils.isNotBlank(join)){
            //删除购物车已经下单的数据
            cartInfoMapper.deleteCartById(join);
        }
        //同步购物车缓存
        flushCartCacheByUserId(userId);
    }

    private boolean isCartCookie(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean flag = true;
        for (CartInfo info : cartInfos) {
            if (info.getSkuId().equals(cartInfo.getSkuId())) {
                flag = false;
            }
        }
        return flag;
    }
}

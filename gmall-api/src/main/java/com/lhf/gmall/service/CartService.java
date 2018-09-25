package com.lhf.gmall.service;

import com.lhf.gmall.bean.CartInfo;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-15 17:05
 */
public interface CartService {
    /**
     * 判断有没有购物车
     * @param cartInfo
     * @return
     */
    CartInfo isCartExist(CartInfo cartInfo);

    /**
     * 更新购物车
     * @param cartInfoDb
     */
    void updateCart(CartInfo cartInfoDb);

    /**
     * 添加购物车
     * @param cartInfo
     */
    void insertCart(CartInfo cartInfo);

    /**
     * 刷新缓存
     * @param userId
     */
    void flushCartCacheByUserId(String userId);

    /**
     * 计算总价
     * @param userId
     * @return
     */
    List<CartInfo> getCartInfosFromCacheByUserId(String userId);

    /**
     * 异步更新购物车列表页面
     * @param cartInfo
     * @return
     */
    void updateCartByUserId(CartInfo cartInfo);

    /**
     * 合并购物车
     * @param id
     * @param cartInfos
     */
    void combineCart(String id, List<CartInfo> cartInfos);

    void deleteCart(String join,String userId);
}

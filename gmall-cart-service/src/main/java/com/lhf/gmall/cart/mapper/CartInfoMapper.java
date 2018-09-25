package com.lhf.gmall.cart.mapper;

import com.lhf.gmall.bean.CartInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author shkstart
 * @create 2018-09-15 17:09
 */
public interface CartInfoMapper extends Mapper<CartInfo>{

    void deleteCartById(@Param(value = "join") String join);
}

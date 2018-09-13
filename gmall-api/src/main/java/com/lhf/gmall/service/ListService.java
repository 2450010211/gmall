package com.lhf.gmall.service;

import com.lhf.gmall.bean.SkuLsInfo;
import com.lhf.gmall.bean.SkuLsParam;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-13 11:02
 */
public interface ListService {
    /**
     * 根据SkuLsParam的属性查询elasticsearch里的数据
     * @param skuLsParam
     * @return
     */
    List<SkuLsInfo> search(SkuLsParam skuLsParam);
}

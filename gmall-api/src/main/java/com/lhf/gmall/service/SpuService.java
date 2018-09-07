package com.lhf.gmall.service;

import com.lhf.gmall.bean.BaseSaleAttr;
import com.lhf.gmall.bean.SpuInfo;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-07 12:01
 */
public interface SpuService {
    /**
     * 查询baseSaleAttr数据
     * @return
     */
    List<BaseSaleAttr> baseSaleAttrList();

    /**
     * 保存销售属性
     * @param spuInfo
     */
    String saveSpu(SpuInfo spuInfo);
}

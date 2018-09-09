package com.lhf.gmall.service;

import com.lhf.gmall.bean.SkuInfo;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-08 22:36
 */
public interface SkuService {

    /**
     * 查询skuList
     * @return
     */
    List<SkuInfo> skuList(String spuId);

    /**
     * 保存skuInfo信息
     */
    void saveSku(SkuInfo skuInfo);
}

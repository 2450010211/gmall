package com.lhf.gmall.service;

import com.lhf.gmall.bean.BaseSaleAttr;
import com.lhf.gmall.bean.SpuImage;
import com.lhf.gmall.bean.SpuInfo;
import com.lhf.gmall.bean.SpuSaleAttr;

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
    void saveSpu(SpuInfo spuInfo);

    /**
     * 查询spuInfo
     * @return
     */
    List<SpuInfo> spuList(String catalog3Id);

    /**
     * 查询销售属性
     * @return
     */
    List<SpuSaleAttr> spuSaleAttrList(String spuId);

    /**
     * 加载图片信息
     * @param spuId
     * @return
     */
    List<SpuImage> spuImageList(String spuId);
}

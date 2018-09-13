package com.lhf.gmall.service;

import com.lhf.gmall.bean.SkuInfo;
import com.lhf.gmall.bean.SpuSaleAttr;

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

    /**
     * 查询sku信息和图片信息
     * @param skuId
     * @return
     */
    SkuInfo getSkuById(String skuId);

    /**
     * 查询销售属性和销售属性值
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String spuId,String skuId);

    /**
     * 查询兄弟sku
     * @param spuId
     * @return
     */
    List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId);

    /**
     * 根据三级分类Id查询数据
     * @param
     * @return
             */
    List<SkuInfo> getSkuByCatalog3Id(int catalog3Id);
}

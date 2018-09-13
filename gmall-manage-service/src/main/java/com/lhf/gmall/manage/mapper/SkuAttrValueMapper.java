package com.lhf.gmall.manage.mapper;

import com.lhf.gmall.bean.SkuAttrValue;
import com.lhf.gmall.bean.SkuInfo;
import com.lhf.gmall.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-09 17:37
 */
public interface SkuAttrValueMapper extends Mapper<SkuAttrValue> {

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param(value = "spuId") String spuId,@Param(value = "skuId") String skuId);

    List<SkuInfo> selectSkuSaleAttrValueListBySpu(@Param(value = "spuId") String spuId);
}

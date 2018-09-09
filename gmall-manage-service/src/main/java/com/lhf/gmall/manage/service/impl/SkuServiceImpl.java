package com.lhf.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lhf.gmall.bean.SkuAttrValue;
import com.lhf.gmall.bean.SkuImage;
import com.lhf.gmall.bean.SkuInfo;
import com.lhf.gmall.bean.SkuSaleAttrValue;
import com.lhf.gmall.manage.mapper.SkuAttrValueMapper;
import com.lhf.gmall.manage.mapper.SkuImageMapper;
import com.lhf.gmall.manage.mapper.SkuInfoMapper;
import com.lhf.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.lhf.gmall.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-08 22:37
 */
@Service
public class SkuServiceImpl implements SkuService{

    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;

    @Override
    public List<SkuInfo> skuList(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        List<SkuInfo> select = skuInfoMapper.select(skuInfo);
        return select;
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();

        //保存平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList!=null && skuAttrValueList.size() != 0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuId);
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

        //保存销售属性的值
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList != null && skuSaleAttrValueList.size() != 0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuId);
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }

        //保存图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size() != 0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuId);
                skuImageMapper.insertSelective(skuImage);
            }
        }
    }
}

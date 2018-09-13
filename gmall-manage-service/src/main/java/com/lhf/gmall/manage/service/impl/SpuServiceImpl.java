package com.lhf.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lhf.gmall.bean.*;
import com.lhf.gmall.manage.mapper.*;
import com.lhf.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-07 12:03
 */
@Service
public class SpuServiceImpl  implements SpuService{

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectAll();
        return baseSaleAttrs;
    }

    @Override
    public void saveSpu(SpuInfo spuInfo) {

        //保存spuInfo
        spuInfoMapper.insertSelective(spuInfo);
        String spuId = spuInfo.getId();

        //保存销售属性表SpuSaleAttr
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuId);
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            //保存销售属性值表SpuSaleAttrValue
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                saleAttrValue.setSpuId(spuId);
                spuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }
        //保存图片表SpuImage
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
           spuImage.setSpuId(spuId);
           spuImageMapper.insertSelective(spuImage);
        }
    }

    @Override
    public List<SpuInfo> spuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> select = spuInfoMapper.select(spuInfo);
        return select;
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        SpuSaleAttr saleAttr = new SpuSaleAttr();
        saleAttr.setSpuId(spuId);
        List<SpuSaleAttr> select = spuSaleAttrMapper.select(saleAttr);

        if(select != null && select.size() != 0){
            for (SpuSaleAttr spuSaleAttr : select) {
                String saleAttrId = spuSaleAttr.getSaleAttrId();
                SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
                //s一定要set两个字段
                spuSaleAttrValue.setSpuId(spuId);
                spuSaleAttrValue.setSaleAttrId(saleAttrId);
                List<SpuSaleAttrValue> select1 = spuSaleAttrValueMapper.select(spuSaleAttrValue);
                spuSaleAttr.setSpuSaleAttrValueList(select1);
            }
        }
        return select;
    }

    @Override
    public List<SpuImage> spuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> select = spuImageMapper.select(spuImage);
        return select;
    }
}

package com.lhf.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lhf.gmall.bean.BaseSaleAttr;
import com.lhf.gmall.bean.SpuInfo;
import com.lhf.gmall.manage.mapper.BaseSaleAttrMapper;
import com.lhf.gmall.manage.mapper.SpuInfoMapper;
import com.lhf.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

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

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectAll();
        return baseSaleAttrs;
    }

    @Override
    public String saveSpu(SpuInfo spuInfo) {
        spuInfoMapper.insertSelective(spuInfo);
        return spuInfo.getId();
    }
}

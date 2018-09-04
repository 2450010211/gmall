package com.lhf.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lhf.gmall.bean.BaseAttrInfo;
import com.lhf.gmall.manage.mapper.BaseAttrInfoMapper;
import com.lhf.gmall.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-04 22:02
 */
@Service
public class AttrServiceImpl implements AttrService{

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Override
    public List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id) {

        return null;
    }
}

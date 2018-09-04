package com.lhf.gmall.service;

import com.lhf.gmall.bean.BaseAttrInfo;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-04 21:59
 */
public interface AttrService {
    List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id);
}

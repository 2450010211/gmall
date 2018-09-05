package com.lhf.gmall.service;

import com.lhf.gmall.bean.BaseAttrInfo;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-04 21:59
 */
public interface AttrService {
    /**
     * 查询属性列表
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id);
    /**
     * 修改并保存属性列表
     * @param baseAttrInfo
     */
    void saveAttr(BaseAttrInfo baseAttrInfo);
}

package com.lhf.gmall.service;

import com.lhf.gmall.bean.BaseAttrInfo;
import com.lhf.gmall.exception.BaseAttrInfoException;

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
    void saveAttr(BaseAttrInfo baseAttrInfo) throws BaseAttrInfoException;

    /**
     * 编辑对话框回显销售属性
     * @return
     */
    List<BaseAttrInfo> getBaseAttrInfo(String catalog3Id);

    /**
     * 查询所有的平台属性值
     * @param join
     * @return
     */
    List<BaseAttrInfo> getAttrListByValueId(String join);
}

package com.lhf.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lhf.gmall.bean.BaseAttrInfo;
import com.lhf.gmall.bean.BaseAttrValue;
import com.lhf.gmall.manage.mapper.BaseAttrInfoMapper;
import com.lhf.gmall.manage.mapper.BaseAttrValueMapper;
import com.lhf.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-04 22:02
 */
@Service
public class AttrServiceImpl implements AttrService{

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> select = baseAttrInfoMapper.select(baseAttrInfo);
        return select;
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        String id = baseAttrInfo.getId();
        if(StringUtils.isBlank(id)){//id不为空 保存
            Example example = new Example(BaseAttrInfo.class);
            example.createCriteria()
                    .andEqualTo("catalog3Id", baseAttrInfo.getCatalog3Id())
                    .andEqualTo("attrName", baseAttrInfo.getAttrName());
            List<BaseAttrInfo> select = baseAttrInfoMapper.selectByExample(example);
            if(select.size()==0){
                baseAttrInfoMapper.insertSelective(baseAttrInfo);
                String attrId  = baseAttrInfo.getId();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue attrValue : attrValueList) {
                    attrValue.setAttrId(attrId);
                    baseAttrValueMapper.insert(attrValue);
                }
            }else{
                String attrId = select.get(0).getId();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue attrValue : attrValueList) {
                    attrValue.setAttrId(attrId);
                    baseAttrValueMapper.insert(attrValue);
                }
            }
        }else{//修改
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
            Example example = new Example(BaseAttrValue.class);
            example.createCriteria().andEqualTo("attrid",baseAttrInfo.getId());
            baseAttrValueMapper.deleteByExample(example);

            String attrid = baseAttrInfo.getId();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setId(attrid);
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }
}

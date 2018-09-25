package com.lhf.gmall.manage.mapper;


import com.lhf.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-04 22:03
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    List<BaseAttrInfo> selectAttrListByValueId(@Param(value = "join") String join);
}

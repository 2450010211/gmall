package com.lhf.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lhf.gmall.bean.BaseAttrInfo;
import com.lhf.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-04 21:55
 */
@Controller
public class AttrController {

    @Reference
    AttrService attrService;

    @RequestMapping(value = "getAttrListByCtg3")
    @ResponseBody
    public List<BaseAttrInfo> getAttrListByCtg3(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");
        List<BaseAttrInfo> baseAttrInfos = attrService.getAttrListByCtg3(catalog3Id);
        return baseAttrInfos;
    }
}

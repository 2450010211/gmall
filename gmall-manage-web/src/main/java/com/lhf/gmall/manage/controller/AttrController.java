package com.lhf.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.lhf.gmall.bean.BaseAttrInfo;
import com.lhf.gmall.exception.BaseAttrInfoException;
import com.lhf.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
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
    public List<BaseAttrInfo> getAttrListByCtg3(@RequestParam Map<String, String> map) {
        String catalog3Id = map.get("catalog3Id");
        List<BaseAttrInfo> baseAttrInfos = attrService.getAttrListByCtg3(catalog3Id);
        return baseAttrInfos;
    }

    @RequestMapping(value = "/saveAttr")
    @ResponseBody
    public String saveAttr(BaseAttrInfo baseAttrInfo) {
        try {
            attrService.saveAttr(baseAttrInfo);
        }catch (BaseAttrInfoException e){
            return e.getMessage();
        }
        return "添加成功";
    }

    @RequestMapping(value = "/getBaseAttrInfo")
    @ResponseBody
    public List<BaseAttrInfo> getBaseAttrInfo(@RequestParam Map<String, String> map){
        String catalog3Id = map.get("catalog3Id");
        List<BaseAttrInfo> baseAttrInfos = attrService.getBaseAttrInfo(catalog3Id);
        return baseAttrInfos;
    }
}

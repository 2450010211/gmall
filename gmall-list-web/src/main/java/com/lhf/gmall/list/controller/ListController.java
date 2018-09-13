package com.lhf.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lhf.gmall.bean.SkuLsInfo;
import com.lhf.gmall.bean.SkuLsParam;
import com.lhf.gmall.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-13 10:25
 */
@Controller
public class ListController {

    @Reference
    ListService listService;

    @RequestMapping(value = "/list.html")
    public String list(SkuLsParam skuLsParam, ModelMap modelMap){
        List<SkuLsInfo> skuLsInfoList =  listService.search(skuLsParam);
        modelMap.put("skuLsInfoList", skuLsInfoList);
        return "list";
    }
}

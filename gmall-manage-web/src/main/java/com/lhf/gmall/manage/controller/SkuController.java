package com.lhf.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lhf.gmall.bean.SkuInfo;
import com.lhf.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-08 22:32
 */
@Controller
public class SkuController {

    @Reference
    SkuService skuService;


    @RequestMapping(value = "/saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){
        skuService.saveSku(skuInfo);
        return "success";
    }

    @RequestMapping(value = "/skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> skuInfoListBySpu(@RequestParam(value = "spuId") String spuId){
        List<SkuInfo> skuInfos = skuService.skuList(spuId);
        return skuInfos;
    }
}

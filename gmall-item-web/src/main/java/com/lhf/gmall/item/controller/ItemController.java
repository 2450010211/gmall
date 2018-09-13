package com.lhf.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.lhf.gmall.bean.SkuInfo;
import com.lhf.gmall.bean.SkuSaleAttrValue;
import com.lhf.gmall.bean.SpuSaleAttr;
import com.lhf.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-10 12:01
 */
@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @RequestMapping(value = "/demo")
    public String demo(ModelMap modelMap){

        ArrayList<String> strings = new ArrayList<>();
        for (int i = 1; i <= 5 ; i++) {
            strings.add("thymeleaf" + i);
        }
        modelMap.put("flag", 1);
        modelMap.put("list", strings);
        return "demo";
    }

    @RequestMapping(value = "{skuId}.html")
    public String index(@PathVariable(value = "skuId") String skuId, ModelMap modelMap){
        SkuInfo skuInfo = skuService.getSkuById(skuId);
        modelMap.put("skuInfo", skuInfo);
        String spuId = skuInfo.getSpuId();
        //查询销售属性列表
        List<SpuSaleAttr> spuSaleAttrs = skuService.getSpuSaleAttrListCheckBySku(spuId,skuInfo.getId());
        modelMap.put("spuSaleAttrListCheckBySku", spuSaleAttrs);
        //查询sku兄弟的hash表的hashMap
        List<SkuInfo> skuInfos = skuService.getSkuSaleAttrValueListBySpu(spuId);
        Map<String, String> skuMap = new HashMap<>();
        for (SkuInfo info : skuInfos) {
            String v = info.getId();
            String k = "";
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                String saleAttrValueId = skuSaleAttrValue.getSaleAttrValueId();
                k =  k + "|" + saleAttrValueId;
            }
            skuMap.put(k, v);
        }
        //用json工具将skuMap转换成json字符串
        String skuMapJson = JSON.toJSONString(skuMap);
        modelMap.put("skuMapJson", skuMapJson);
        System.out.println(skuMapJson);
        return "item";
    }
}

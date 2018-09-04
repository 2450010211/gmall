package com.lhf.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lhf.gmall.bean.BaseCatalog1;
import com.lhf.gmall.bean.BaseCatalog2;
import com.lhf.gmall.bean.BaseCatalog3;
import com.lhf.gmall.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-04 19:21
 */
@Controller
public class CatalogController {

    @Reference
    CatalogService catalogService;

    @RequestMapping(value = "/getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        List<BaseCatalog1> baseCatalog1s = catalogService.getCatalog1();
        return baseCatalog1s;
    }

    @RequestMapping(value = "/getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(@RequestParam Map<String,String> map){
        String catalog1Id = map.get("catalog1Id");
        List<BaseCatalog2> baseCatalog2s = catalogService.getCatalog2(catalog1Id);
        return baseCatalog2s;
    }

    @RequestMapping(value = "/getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(@RequestParam Map<String,String> map){
        String catalog2Id = map.get("catalog2Id");
        List<BaseCatalog3> baseCatalog3s = catalogService.getCatalog3(catalog2Id);
        return baseCatalog3s;
    }
}

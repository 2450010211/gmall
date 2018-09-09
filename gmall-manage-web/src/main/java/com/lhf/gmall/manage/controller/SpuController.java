package com.lhf.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lhf.gmall.bean.*;
import com.lhf.gmall.manage.util.FileUploadFile;
import com.lhf.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-07 11:52
 */
@Controller
public class SpuController {

    @Reference
    SpuService spuService;


    @RequestMapping(value = "/spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(@RequestParam(value = "spuId") String spuId){
        List<SpuImage> spuImages = spuService.spuImageList(spuId);
        return spuImages;
    }

    @RequestMapping(value = "/spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrList(@RequestParam(value = "spuId") String spuId){
        List<SpuSaleAttr> spuSaleAttrs = spuService.spuSaleAttrList(spuId);
        return  spuSaleAttrs;
    }

    @RequestMapping(value = "/baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = spuService.baseSaleAttrList();
        return baseSaleAttrs;
    }

    @RequestMapping(value = "/saveSpu")
    @ResponseBody
    public String saveSpu(SpuInfo spuInfo){
        spuService.saveSpu(spuInfo);
        return "success";
}

    @RequestMapping(value = "/fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file) {
        String imgUrl = FileUploadFile.uploadImage(file);
        return imgUrl;
    }

    @RequestMapping(value = "/spuList")
    @ResponseBody
    public List<SpuInfo> spuList(@RequestParam(value = "catalog3Id") String catalog3Id){
        List<SpuInfo> spuInfos = spuService.spuList(catalog3Id);
        return spuInfos;
    }

}

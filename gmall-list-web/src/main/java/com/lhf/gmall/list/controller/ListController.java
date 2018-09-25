package com.lhf.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lhf.gmall.bean.*;
import com.lhf.gmall.service.AttrService;
import com.lhf.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-13 10:25
 */
@Controller
public class ListController {


    @Reference
    ListService listService;
    @Reference
    AttrService attrService;

    @RequestMapping(value = "/list.html")
    public String list(SkuLsParam skuLsParam, ModelMap modelMap){
        List<SkuLsInfo> skuLsInfoList =  listService.search(skuLsParam);

        //查询所有的平台属性值
        HashSet<String> hashSet = new HashSet<>();
        for (SkuLsInfo skuLsInfo : skuLsInfoList) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                //去重
                hashSet.add(valueId);
            }
        }
        String join = StringUtils.join(hashSet, ",");
        List<BaseAttrInfo> baseAttrInfos = attrService.getAttrListByValueId(join);

        //制作面包屑
        List<Crumb> crumbList = new ArrayList<>();
        //删除当前请求中所包含的属性
        String[] valueId = skuLsParam.getValueId();
        if(null != valueId && valueId.length > 0){
            for (String sid : valueId) {
                //制作面包屑对象
                Crumb crumb = new Crumb();
                //排除属性
                Iterator<BaseAttrInfo> iterator = baseAttrInfos.iterator();
                while (iterator.hasNext()){
                    BaseAttrInfo baseAttrInfo = iterator.next();
                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                    //删除当前的valueId所 关联的属性
                    for (BaseAttrValue baseAttrValue : attrValueList) {
                        String id = baseAttrValue.getId();
                        if(id.equals(sid)){
                            //设置面包屑的名称
                            crumb.setValueName(baseAttrValue.getValueName());
                            iterator.remove();
                        }
                    }
                }
                String crumbUrlParam = getUrlParam(skuLsParam,sid);
                crumb.setUrlParam(crumbUrlParam);
                crumbList.add(crumb);
            }
        }
        //加载商品的sku
        modelMap.put("skuLsInfoList", skuLsInfoList);
        //显示平台属性属性值
        modelMap.put("attrList",baseAttrInfos);
        //通过选择平台属性值筛选商品
        String urlParam = getUrlParam(skuLsParam);
        //平台属性筛选
        modelMap.put("urlParam", urlParam);
        //面包屑
        modelMap.put("attrValueSelectedList", crumbList);
        return "list";
    }


    private String getUrlParam(SkuLsParam skuLsParam) {
        //拼接请求
        String[] valueId = skuLsParam.getValueId();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();

        String urlParam = "";
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if(null != valueId && valueId.length > 0){
            for (String id : valueId) {
                urlParam = urlParam + "&"+ "valueId=" + id;
            }
        }
        return urlParam;
    }

    private String getUrlParam(SkuLsParam skuLsParam, String sid) {
        //拼接请求
        String[] valueId = skuLsParam.getValueId();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();

        String urlParam = "";
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if(null != valueId && valueId.length > 0){
            for (String id : valueId) {
                //面包屑请求地址=当前请求(浏览器地址栏)-面包屑所代表的平台属性
                if(!id.equals(sid)){
                    urlParam = urlParam + "&"+ "valueId=" + id;
                }
            }
        }
        return urlParam;
    }
}

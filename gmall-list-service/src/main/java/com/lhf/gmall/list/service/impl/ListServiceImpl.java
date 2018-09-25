package com.lhf.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lhf.gmall.bean.SkuLsInfo;
import com.lhf.gmall.bean.SkuLsParam;
import com.lhf.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-13 11:04
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;
    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {

        //System.out.println(getMyDsl(skuLsParam));

        Search build = new Search.Builder(getMyDsl(skuLsParam)).addIndex("gmall").addType("SkuLsInfo").build();

        List<SkuLsInfo> skuLsInfos = new ArrayList<>();

        //执行语句
        try {
            SearchResult execute = jestClient.execute(build);
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo source = hit.source;
                //关键字高亮
                Map<String, List<String>> highlight = hit.highlight;
                if(highlight != null && highlight.size() > 0){
                    List<String> skuName = highlight.get("skuName");
                    String skuNameHl = skuName.get(0);
                    //替换
                    source.setSkuName(skuNameHl);
                }
                skuLsInfos.add(source);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuLsInfos;
    }


    public String getMyDsl(SkuLsParam skuLsParam){

        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueId = skuLsParam.getValueId();

        // query -- bool -- filter -- term   must -- match
        //定义一个查询语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if(StringUtils.isNotBlank(catalog3Id)){
            //过滤
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParam.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if(null!=valueId&&valueId.length > 0){
            //加载平台分类属性
            for (int i = 0; i < valueId.length; i++) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId[i]);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        if(StringUtils.isNotBlank(keyword)){
            //查询
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder skuName = highlightBuilder.field("skuName");
        skuName.preTags("<span style='color:red'>");
        skuName.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);
        return searchSourceBuilder.toString();
    }
}

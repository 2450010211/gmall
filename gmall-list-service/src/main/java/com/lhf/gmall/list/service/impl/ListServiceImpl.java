package com.lhf.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lhf.gmall.bean.SkuLsInfo;
import com.lhf.gmall.bean.SkuLsParam;
import com.lhf.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        System.out.println(getMyDsl(skuLsParam));

        Search build = new Search.Builder(getMyDsl(skuLsParam)).addIndex("gmall").addType("SkuLsInfo").build();

        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
        //执行语句
        try {
            SearchResult execute = jestClient.execute(build);
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo source = hit.source;
                skuLsInfos.add(source);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuLsInfos;
    }

    public String getMyDsl(SkuLsParam skuLsParam){
        // query -- bool -- filter -- term   must -- match
        //定义一个查询语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //过滤
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParam.getCatalog3Id());
        boolQueryBuilder.filter(termQueryBuilder);

        //查询
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","荣耀");
        boolQueryBuilder.must(matchQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);
        return searchSourceBuilder.toString();
    }
}

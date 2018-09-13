package com.lhf.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lhf.gmall.bean.SkuInfo;
import com.lhf.gmall.bean.SkuLsInfo;
import com.lhf.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

    @Autowired
    DataSource dataSource;
    @Autowired
    JestClient jestClient;
    @Reference
    SkuService skuService;

    public static void main(String[] args) {

    }

    @Test
    public void contextLoads() throws IOException {

        System.out.println(getMyDsl());

        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
        ////使用工具类编写DSL语句
        Search build = new Search.Builder(getMyDsl()).addIndex("gmall").addType("SkuLsInfo").build();
        SearchResult execute = jestClient.execute(build);
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo source = hit.source;
            skuLsInfos.add(source);
        }
        System.out.println(skuLsInfos.size());
    }

    //使用工具类编写DSL语句
    public String getMyDsl(){
        //query--bool--filter--term     must--match
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //query <-- bool BoolQueryBuilder()
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter <-- term TermQueryBuilder 过滤
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", 61);
        //全部放入boolQueryBuilder
        boolQueryBuilder.filter(termQueryBuilder);

        //must <-- match  搜索
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "荣耀");
        boolQueryBuilder.must(matchQueryBuilder);
        //属性参数
        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);

        return searchSourceBuilder.toString();
    }

    public void addData() {
        //查询skuInfo
        List<SkuInfo> skuInfoList = skuService.getSkuByCatalog3Id(61);

        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
        //将查询到的skuInfo数据转换成skuLsInfo
        for (SkuInfo skuInfo : skuInfoList) {
            SkuLsInfo skuLsInfo = new SkuLsInfo();
            //只要有相同的字段自动注入
            BeanUtils.copyProperties(skuInfo, skuLsInfo);

            skuLsInfos.add(skuLsInfo);
        }
        //将skuLsInfo中的数据插入到elasticsearch中
        //System.out.println(skuLsInfos);
        //index 插入 select查询
        for (SkuLsInfo skuLsInfo : skuLsInfos) {
            Index build = new Index.Builder(skuLsInfo).index("gmall").type("SkuLsInfo").id(skuLsInfo.getId()).build();
            try {
                jestClient.execute(build);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

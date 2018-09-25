package com.lhf.gmall;

import com.alibaba.fastjson.JSON;
import com.lhf.gmall.bean.BaseCatalog2;
import com.lhf.gmall.bean.BaseCatalog3;
import com.lhf.gmall.manage.mapper.BaseCatalog1Mapper;
import com.lhf.gmall.manage.mapper.BaseCatalog2Mapper;
import com.lhf.gmall.manage.mapper.BaseCatalog3Mapper;
import com.lhf.gmall.util.RedisUtil;
import com.lhf.gmall.bean.BaseCatalog1;
import com.lhf.gmall.service.CatalogService;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import javassist.tools.reflect.ClassMetaobject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageServiceApplicationTests {

	@Autowired
	CatalogService catalogService;
	@Autowired
	RedisUtil redisUtil;
	@Autowired
	BaseCatalog1Mapper baseCatalog1Mapper;
	@Autowired
	BaseCatalog2Mapper baseCatalog2Mapper;
	@Autowired
	BaseCatalog3Mapper baseCatalog3Mapper;

	@Test
	public void contextLoads() {
		List<BaseCatalog1> catalog1 = catalogService.getCatalog1();
		for (int i = 0; i < catalog1.size(); i++) {
			System.out.println(catalog1.get(i).getName().length());
		}
	}
	@Test
	public void redisTest(){
        Jedis jedis = redisUtil.getJedis();
        String ping = jedis.ping();
        System.out.println(ping);
        jedis.set("zxc", "abc");
    }


	//查询三级分类Id
	@Test
	public void sortTest(){

		List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();
        Map<String,List<BaseCatalog2>> map = new HashMap<>();
		for (BaseCatalog1 baseCatalog1 : baseCatalog1List) {
            String catalog1Id = baseCatalog1.getId();
            BaseCatalog2 baseCatalog2 = new BaseCatalog2();
            baseCatalog2.setCatalog1Id(catalog1Id);
            List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
            map.put(baseCatalog1.getId(), baseCatalog2List);
            for (BaseCatalog2 catalog2 : baseCatalog2List) {
                String catalog2Id = catalog2.getId();
                BaseCatalog3 baseCatalog3 = new BaseCatalog3();
                baseCatalog3.setCatalog2Id(catalog2Id);
                List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
                catalog2.setCatalog3List(baseCatalog3List);
            }
        }
        String s = JSON.toJSONString(map);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File("D://json.json"));
            fileOutputStream.write(s.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.lhf.gmall;

import com.lhf.gmall.util.RedisUtil;
import com.lhf.gmall.bean.BaseCatalog1;
import com.lhf.gmall.service.CatalogService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageServiceApplicationTests {

	@Autowired
	CatalogService catalogService;
	@Autowired
	RedisUtil redisUtil;
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
}

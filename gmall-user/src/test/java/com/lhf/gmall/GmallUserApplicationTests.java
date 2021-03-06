package com.lhf.gmall;

import com.lhf.gmall.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallUserApplicationTests {
	@Autowired
	RedisUtil redisUtil;

	@Test
	public void contextLoads() {
		Jedis jedis = redisUtil.getJedis();
		String ping = jedis.ping();
		System.out.println(ping);
	}

}

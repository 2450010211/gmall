package com.lhf.gmall;

import com.lhf.gmall.util.ActiveMQUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.Connection;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

	@Autowired
	ActiveMQUtil activeMQUtil;

	@Test
	public void contextLoads() {
        Connection connection = activeMQUtil.getConnection();
        System.err.println(connection);
    }

}
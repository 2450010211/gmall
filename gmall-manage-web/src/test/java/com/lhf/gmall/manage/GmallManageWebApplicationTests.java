package com.lhf.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

	@Test
	public void contextLoads() throws IOException, MyException {
        String path = GmallManageWebApplicationTests.class.getClassLoader().getResource("tracker.conf").getPath();

        ClientGlobal.init(path);

        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = trackerClient.getConnection();

        StorageServer storageServer = null;
        StorageClient storageClient = new StorageClient(connection,storageServer);

        String[] pngs = storageClient.upload_file("D://Image.png", "png", null);
        String urlImg = "http://192.168.234.128";
        for (String png : pngs) {

           urlImg = urlImg + "/" + png;
        }
            System.out.println(urlImg);
    }
}

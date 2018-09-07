package com.lhf.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author shkstart
 * @create 2018-09-08 0:34
 */
public class FileUploadFile {

    public static String uploadImage(MultipartFile file) {
        String path = FileUploadFile.class.getClassLoader().getResource("tracker.conf").getPath();

        try {
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StorageServer storageServer = null;
        StorageClient storageClient = new StorageClient(connection, storageServer);

        String[] pngs = null;
        try {
            String originalFilename = file.getOriginalFilename();
            String[] split = originalFilename.split("\\.");
            String extName = split[(split.length - 1)];
            pngs = storageClient.upload_file(file.getBytes(), extName, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        String imgUrl = "http://192.168.234.128";
        for (String png : pngs) {
            imgUrl = imgUrl + "/" + png;
        }
        return imgUrl;
    }
}

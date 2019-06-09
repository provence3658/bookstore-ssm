package com.bookstore.service.Impl;

import com.bookstore.service.IFileService;
import com.bookstore.util.FTPUtil;
import com.bookstore.util.PropertiesUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private static  final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件，上传文件的文件名：{}，上传的路径：{}，新文件名：{}",fileName, path, uploadFileName);

        File folder = new File(path);
        if (!folder.exists()) {
            folder.setWritable(true);
            folder.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);

        try {
            file.transferTo(targetFile);
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            targetFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String imageUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
        return imageUrl;

    }
}

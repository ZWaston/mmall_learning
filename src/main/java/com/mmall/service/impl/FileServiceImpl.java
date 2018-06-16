package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("fileServiceImpl")
public class FileServiceImpl implements IFileService{
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file,String path){
        String fileName = file.getOriginalFilename();
        //扩展名，如fileName = "abc.jpg"，获取到"jpg"
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        //使用随机uuid作为图片名，防止冲突
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);

        //判断文件夹path是否存在
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);//确保有权限写
            fileDir.mkdirs();
        }
        //targetFile保存了 在服务器上的名称和路径
        File targetFile = new File(path,uploadFileName);

        try {
            //将file对象的名字和路径转成targetFile的，涉及到复制和传输，上传的文件会被复制到File所指向的目录(webapp/upload)
            file.transferTo(targetFile);
            //文件上传成功

            //将targetFile上传到ftp服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传到ftp服务器上

            //上传完之后，删除upload文件夹下面的文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();

    }
}

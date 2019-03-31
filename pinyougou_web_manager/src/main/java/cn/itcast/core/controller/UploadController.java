package cn.itcast.core.controller;

import entity.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file)  {
        try {
            //创建一个fastDFS客户端对象
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            //获取文件后缀名
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            //上传,获取文件路径
            String path = fastDFSClient.uploadFile(file.getBytes(), ext, null);
            return new Result(true,FILE_SERVER_URL+path);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }

    }
}

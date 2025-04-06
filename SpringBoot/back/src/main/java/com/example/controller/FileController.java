package com.example.controller;

/*
 * 文件相关的接口
 */
import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import com.example.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {
    //System.getProperty("user.dir")获取当钱项目的根路径
    //文件上传目录的路径
    private static final String filePath = System.getProperty("user.dir") + "/files/";

    /*
    * 文件上传,图片大小小于1M*/
    @PostMapping("/upload")
    public Result upload(MultipartFile file){//文件流的形式接受前端上传的文件
        String originalFilename = file.getOriginalFilename();
        if(!FileUtil.isDirectory(filePath)){//判断文件目录是否存在，不存在则创建
            FileUtil.mkdir(filePath);
        }//提供文件存储的完整路径
        //给文件名加一个唯一的标识
        String fileName = System.currentTimeMillis() + "-" + originalFilename;
        String realPath = filePath + fileName;//完整的文件路径
        try {
           FileUtil.writeBytes(file.getBytes(), realPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("500","文件上传失败");
        }
        //返回一个网络路径
        //http://localhost:8080/files/upload/2021/01/01/123.jpg
        String url = "http://localhost:9090/files/download/" + fileName;
        return Result.success(url);
    }

    /*
    * 文件下载*/
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response){
        try {
            //设置响应头信息，统一编码
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");
           OutputStream os =response.getOutputStream();
           String realPath = filePath + fileName;
           //获取文件的字节数组
           byte[] bytes = FileUtil.readBytes(realPath);
           os.write(bytes);
           os.flush();
           os.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("500","文件下载失败");
        }
    }

    /**
     * wang-editor编辑器文件上传接口
     */
    @PostMapping("/wang/upload")
    public Map<String, Object> wangEditorUpload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (!FileUtil.isDirectory(filePath)) {
            FileUtil.mkdir(filePath);  // 创建一个 files 目录
        }
        // 提供文件存储的完整的路径
        // 给文件名 加一个唯一的标识
        String fileName = System.currentTimeMillis() + "_" + originalFilename;  // 156723232322_xxx.png
        String realPath = filePath + fileName;   // 完整的文件路径
        try {
            FileUtil.writeBytes(file.getBytes(), realPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("500", "文件上传失败");
        }
        String url = "http://localhost:9090/files/download/" + fileName;
        // wangEditor上传图片成功后， 需要返回的参数
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> urlMap = new HashMap<>();
        urlMap.put("url", url);
        list.add(urlMap);
        resMap.put("errno", 0);
        resMap.put("data", list);
        return resMap;
    }



}

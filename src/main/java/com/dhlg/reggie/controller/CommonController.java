package com.dhlg.reggie.controller;

import com.dhlg.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;



@RestController
@RequestMapping("/common")
public class CommonController {

    /**
     * 设置配置文件中的上传路径,修改路径只需要修改配置文件
     */
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){

        //file.getOriginalFilename()获取文件初始文件名
        String originalFilename = file.getOriginalFilename();
        //originalFileName.substring(originalFileName.LastIndexOf("."))获取文件名后缀".jpg"

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //UUID.randomUUID().toString()重新生成文件名,防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID() + suffix;

        //创建目录对象
        File dir = new File(basePath);

        //进行判断是否存在指定位置,不存在进行创建
        if(!dir.exists()){
            dir.mkdir();
        }

        //将临时文件转存到制定位置
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //输入流,通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream((new File(basePath+name)));
            //输出流,通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("img/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ( (len = fileInputStream.read(bytes)) != -1 ){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}

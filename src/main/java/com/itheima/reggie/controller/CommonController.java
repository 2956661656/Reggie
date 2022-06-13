package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 文件上传和下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.upload-path}")
    private String path;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<StringBuilder> upload(@RequestPart("file") MultipartFile file){
        log.info("上传的文件：{}", file.getOriginalFilename());

        String randomName = UUID.randomUUID().toString();
        StringBuilder filename = new StringBuilder();
        for (String s : randomName.split("-")) {
            filename.append(s);
        }

        String originalFilename = file.getOriginalFilename();

        int lastIndex = originalFilename.lastIndexOf(".");
        String suffix = originalFilename.substring(lastIndex);

        File dir = new File(path);
        if (!dir.exists()){
            dir.mkdir();    //目录不存在
        }
        try {
            //将临时文件转存到本地
            file.transferTo(new File(path + filename + suffix));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(filename.append(suffix));
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        ServletOutputStream outputStream = null;

        try {
            //输入流读取文件内容
            fileInputStream = new FileInputStream(new File(path + name));
            bufferedInputStream = new BufferedInputStream(fileInputStream);

            //输出流将文件写回浏览器，展示图片
            response.setContentType("image/jpeg");  //设置响应内容

            outputStream = response.getOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = bufferedInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0, length);
                outputStream.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (bufferedInputStream != null){
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

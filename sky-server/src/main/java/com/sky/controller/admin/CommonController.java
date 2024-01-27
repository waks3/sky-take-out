package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("upload")
    @ApiOperation("文件上传")
    public Result<String>upload(MultipartFile file) throws IOException {
        log.info("文件上传：{}",file);
        try{
            //首先获取上传文件的名字
            String originalFilename= file.getOriginalFilename();
            //截取这个文件的格式，捕获从最后一个.后面
            String extension=originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造新文件名称,就是将生成的UUID后面补充上之前上传文件的格式
            String objectName= UUID.randomUUID().toString()+extension;
            //利用阿里云文档中的代码来生成出文件的请求路径
            String filePath=aliOssUtil.upload(file.getBytes(),objectName);

            return Result.success(filePath);
        }catch (IOException e)
        {
            log.info("文件上传失败：{}",e);
        }
        return Result.error("文件上传失败");
    }
}

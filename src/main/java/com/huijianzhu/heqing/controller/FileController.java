package com.huijianzhu.heqing.controller;

import com.huijianzhu.heqing.utils.DownloadUtil;
import com.huijianzhu.heqing.vo.SystemResult;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.UUID;

/**
 * ================================================================
 * 说明：文件上传请求接口控制器
 * <p>
 * 作者          时间                    注释
 * 刘梓江    2020/5/9  13:50            创建
 * =================================================================
 **/
@Controller
@CrossOrigin //支持跨域
@RequestMapping("/file")
public class FileController {

    /**
     * 文件上传
     * @param file  文件对象
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public SystemResult fileUpload(@RequestParam("file") MultipartFile file) throws  Exception{
        //获取对应的当前项目target目录下对应的files
        File upload = new File(new File(ResourceUtils.getURL("classpath:").getPath()).getAbsolutePath(),"files/");
        if(!upload.exists()){
            upload.mkdirs();
        }
        if(file!=null){
            //存放上传文件的文件夹
            String oldName = file.getOriginalFilename();
            //获取新的名字
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."),oldName.length());

            //构建真实的文件路径
            File newFile = new File(upload + File.separator + newName);

            //转存文件到指定路径，如果文件名重复的话，将会覆盖掉之前的文件,这里是把文件上传到 “绝对路径”
            file.transferTo(newFile);

            return SystemResult.ok(newName);
        }
        return SystemResult.build(500,"上传失败重新上传");
    }

    /**
     * 查看文件
     * @param fileName  文件名称
     * @return
     */
    @PostMapping("/show")
    public void fileShow(String fileName, HttpServletResponse response)  throws  Exception{
        //获取对应的当前项目target目录下对应的files
        File upload = new File(new File(ResourceUtils.getURL("classpath:").getPath()).getAbsolutePath(),"files/"+fileName);
        new DownloadUtil().prototypeDownload(upload,fileName,response,false);
    }
}
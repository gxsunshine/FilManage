package com.gx.demo.controller;

import com.gx.demo.model.FileInfo;
import com.gx.demo.model.Message;
import com.gx.demo.model.PageUtil;
import com.gx.demo.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: gx
 * @Date: Created in 2020/3/12 15:16
 * @Description: Hello Controller类
 */
@Controller
//@RequestMapping("file")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    @Value("${filepath}")
    private String filepath;
    @Value("${pageSize}")
    private int pageSize;

    @RequestMapping(value = "/hello")
    public String uploading() {
        return "login";
    }

    /**
     * 处理文件上传
     */
    @RequestMapping(value = "/upload")
    public ModelAndView uploading(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        File targetFile = new File(filepath);
        Message message = new Message();
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        File[] files = targetFile.listFiles();
        for(File diskFile: files){
            if(diskFile.getName().equals(file.getOriginalFilename())){
                message.setError("文件上传失败！文件名重复，该文件已上传；建议修改文件名后继续上传，或者删除已上传的文件");
                return show(request, message);
            }
        }
        try (FileOutputStream out = new FileOutputStream(filepath + file.getOriginalFilename());){
            out.write(file.getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("文件上传失败!");
            message.setError("文件上传失败!");
            throw new RuntimeException(e);
        }
        logger.info("文件上传成功!");
        message.setSuccess("文件上传成功!");
        return show(request, message);
    }

    @RequestMapping(value = "del")
    public ModelAndView del(HttpServletRequest request){
        String delFileName = request.getParameter("delFileName");
        Message message = new Message();
        if(!StringUtils.isEmpty(delFileName)){
            File delFile = new File(filepath + delFileName);
            delFile.delete();
            message.setSuccess("文件删除成功!");
        }
        return show(request, message);
    }

    @RequestMapping(value = "/show")
    public ModelAndView show(HttpServletRequest request, Message message) {
        String currentPage = request.getParameter("currentPage");
        PageUtil pageUtil = new PageUtil();
        if(this.pageSize != 0){
            pageUtil.setPageSize(this.pageSize);
        }
        if(!StringUtils.isEmpty(currentPage)){
            pageUtil.setCurrentPage(Integer.parseInt(currentPage));
        }
        File targetFile = new File(filepath);
        File[] files = targetFile.listFiles();
        if(files.length == 0){
            pageUtil.setSumPage(0);
        }else{
            int sumPage = (files.length / pageUtil.getPageSize());
            pageUtil.setSumPage(files.length % pageUtil.getPageSize() == 0 ? sumPage : sumPage + 1);
        }
        List<FileInfo> fileInfos = new ArrayList<FileInfo>();
        for(int i = pageUtil.getCurrentPage() * pageUtil.getPageSize(); (i < (pageUtil.getCurrentPage()+1) * pageUtil.getPageSize()) && (i < files.length); i++){
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(files[i].getName());
            fileInfo.setFileSize(FileUtils.getFormatSize(files[i].length()));
            fileInfos.add(fileInfo);
        }

        ModelAndView modelAndView = new ModelAndView("fileList");
        modelAndView.getModel().put("fileInfos", fileInfos);
        modelAndView.getModel().put("pageUtil", pageUtil);
        modelAndView.getModel().put("message", message);
        return modelAndView;
    }

    @RequestMapping("/download")
    public void downLoad(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {
        String filename= request.getParameter("fileName");
        File file = new File(filepath + filename);
        if(file.exists()){
            response.setContentType("application/octet-stream");
            response.setHeader("content-type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(filename,"utf8"));
            byte[] buffer = new byte[1024];
            //输出流
            OutputStream os = null;
            try(FileInputStream fis= new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);) {
                os = response.getOutputStream();
                int i = bis.read(buffer);
                while(i != -1){
                    os.write(buffer);
                    i = bis.read(buffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

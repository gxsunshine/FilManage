package com.gx.demo.controller;

import com.gx.demo.common.PromptMessage;
import com.gx.demo.model.FileInfo;
import com.gx.demo.model.Message;
import com.gx.demo.model.PageUtil;
import com.gx.demo.utils.FileUtils;
import org.apache.catalina.Session;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.text.MessageFormat;
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
    @Value("${upload.file.max.size}")
    private Long uploadFileSize;

    @RequestMapping(value = "/hello")
    public String uploading() {
        return "login";
    }

    /**
     * 处理文件上传
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST ,headers = "content-type=multipart/*")
    public String uploading(@RequestParam("file") MultipartFile file, HttpServletRequest request) {

        HttpSession session = request.getSession();
        File targetFile = new File(filepath);
        Message message = new Message();
        if(file.getSize() > uploadFileSize){
            logger.error(PromptMessage.UPLOAD_FILE_SIZE_EXCEED_LIMIT);
            message.setError(PromptMessage.UPLOAD_FILE_SIZE_EXCEED_LIMIT);
            session.setAttribute("message", message);
            return "redirect:/show";
        }
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        File[] files = targetFile.listFiles();
        for(File diskFile: files){
            if(diskFile.getName().equals(file.getOriginalFilename())){
                logger.error(PromptMessage.UPLOAD_FILE_NAME_REPEAT);
                message.setError(PromptMessage.UPLOAD_FILE_NAME_REPEAT);
                session.setAttribute("message", message);
                return "redirect:/show";
            }
        }
        byte[] bytes = new byte[1024];
        InputStream fis = null;
        FileOutputStream out = null;
        try{
            out = new FileOutputStream(filepath + file.getOriginalFilename());
            fis = file.getInputStream();
            // 分批写入内存，然后写入文件。
            while (fis.read(bytes) != -1){
                out.write(bytes);
            }
            // 一次性把文件写入内存，容易造成内存溢出
//            out.write(file.getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(PromptMessage.FILE_UPLOAD_FAIL);
            message.setError(PromptMessage.FILE_UPLOAD_FAIL);
//            throw new RuntimeException(e);
        } finally {
            try {
                if(fis != null){
                    fis.close();
                }
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(PromptMessage.CLOSE_FILE_STREAM_FAIL);
            }
        }
        logger.info(PromptMessage.FILE_UPLOAD_SUCCESS);
        message.setSuccess(PromptMessage.FILE_UPLOAD_SUCCESS);
        session.setAttribute("message", message);
        return "redirect:/show";
    }

    @RequestMapping(value = "del")
    public String del(HttpServletRequest request){
        HttpSession session = request.getSession();
        String delFileName = request.getParameter("delFileName");
        Message message = new Message();
        if(!StringUtils.isEmpty(delFileName)){
            File delFile = new File(filepath + delFileName);
            delFile.delete();
            logger.info(PromptMessage.FILE_DELETE_SUCCESS);
            message.setSuccess(PromptMessage.FILE_DELETE_SUCCESS);
            session.setAttribute("message", message);
        }
        return "redirect:/show";
    }

    @RequestMapping(value = "/show")
    public ModelAndView show(HttpServletRequest request) {
        String currentPage = "0";
        if(request != null){
            currentPage = request.getParameter("currentPage");
        }

        PageUtil pageUtil = new PageUtil();
        if(this.pageSize != 0){
            pageUtil.setPageSize(this.pageSize);
        }
        if(!StringUtils.isEmpty(currentPage)){
            pageUtil.setCurrentPage(Integer.parseInt(currentPage));
        }
        File targetFile = new File(filepath);
        File[] files = targetFile.listFiles();
        List<FileInfo> fileInfos = new ArrayList<FileInfo>();
        if(files == null || files.length == 0){
            pageUtil.setSumPage(0);
        }else{
            int sumPage = (files.length / pageUtil.getPageSize());
            pageUtil.setSumPage(files.length % pageUtil.getPageSize() == 0 ? sumPage : sumPage + 1);
            for(int i = pageUtil.getCurrentPage() * pageUtil.getPageSize(); (i < (pageUtil.getCurrentPage()+1) * pageUtil.getPageSize()) && (i < files.length); i++){
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(files[i].getName());
                fileInfo.setFileSize(FileUtils.getFormatSize(files[i].length()));
                fileInfos.add(fileInfo);
            }
        }
        HttpSession session = request.getSession();
        Message message = (Message)session.getAttribute("message");
        ModelAndView modelAndView = new ModelAndView("fileList");
        modelAndView.getModel().put("fileInfos", fileInfos);
        modelAndView.getModel().put("pageUtil", pageUtil);
        if(message != null){
            modelAndView.getModel().put("message", message);
            session.removeAttribute("message");
        }
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

    @RequestMapping("/exception")
    public void exception(){
        throw new ArrayIndexOutOfBoundsException("数据越界了");
//        try {
//            throw new SizeLimitExceededException("exception", 100L ,200L);
//        } catch (SizeLimitExceededException e) {
//            e.printStackTrace();
//        }
    }
}

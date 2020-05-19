package com.gx.demo.config;

import com.gx.demo.common.PromptMessage;
import com.gx.demo.model.Message;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @ClassName CustomExceptionHandler
 * @Description 自定义异常处理器 -- 捕获项目抛出的异常并进行处理
 * @Authtor guoxiang
 * @Date 2020/5/18 15:38
 **/

@ControllerAdvice
public class CustomExceptionHandler {
    /**
     * 捕获 SizeLimitExceededException， 但是没有效果。待研究
     * @param e
     * @param request
     * @return
     * @throws IOException
     */
    @ExceptionHandler(SizeLimitExceededException.class)
    public String uploadException(SizeLimitExceededException e, HttpServletRequest request) throws IOException {
        HttpSession session = request.getSession();
        System.out.println("SizeLimitExceededException+ SizeLimitExceededException  SizeLimitExceededException");
        Message message = new Message();
        if(session != null){
            message.setError(PromptMessage.UPLOAD_FILE_SIZE_EXCEED_LIMIT);
            session.setAttribute("message", message);
        }
        return "redirect:/show";
    }

    /**
     * 捕获 MaxUploadSizeExceededException 异常（spring.servlet.multipart.maxFileSize）触发
     * 可以正常捕获，但是文件比较大（超过10M），页面重定向不正常，1M 是正常的
     * @param e
     * @param request
     * @return
     * @throws IOException
     */
    @ExceptionHandler(MultipartException.class)
//    @ResponseBody
    public String uploadException(MaxUploadSizeExceededException e, HttpServletRequest request) throws IOException {
        e.printStackTrace();
        HttpSession session = request.getSession();
        System.out.println("MaxUploadSizeExceededException + MaxUploadSizeExceededException ");
        Message message = new Message();
        if(session != null){
            message.setError(PromptMessage.UPLOAD_FILE_SIZE_EXCEED_LIMIT);
            session.setAttribute("message", message);
        }
        return "redirect:/show";
    }

//    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
//    public String uploadException(ArrayIndexOutOfBoundsException e, HttpServletRequest request) throws IOException {
//        e.printStackTrace();
//        System.out.println("进入数组越界异常捕获！");
//        return "index";
//    }
}

package com.gx.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
  *@ClassName IndexController
  *@Description 首页Controller
  *@Authtor guoxiang
  *@Date 2020/5/19 10:17
 **/
@Controller
public class IndexController {
    /**
     * 指定首页直接重定向到show
     * @param request
     * @return
     */
    @RequestMapping(value = "/")
    public String uploading(HttpServletRequest request) {
        return "redirect:/show";
    }
}

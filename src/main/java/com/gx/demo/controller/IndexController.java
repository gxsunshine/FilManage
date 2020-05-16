package com.gx.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: gx
 * @Date: Created in 2020/3/12 15:16
 * @Description: Hello Controller类
 */
@Controller
public class IndexController {

    @RequestMapping(value = "/")
    public String uploading(HttpServletRequest request) {
        return "redirect:/show";
    }
}

package com.lhf.gmall.list.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author shkstart
 * @create 2018-09-13 10:25
 */
@Controller
public class IndexController {

    @RequestMapping(value = "/index")
    public String index(){

        return "index";
    }
}

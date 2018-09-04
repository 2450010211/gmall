package com.lhf.gmall.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author shkstart
 * @create 2018-09-04 11:40
 */
@Controller
public class IndexController {

    @RequestMapping(value = "/index")
    public String index(){
        return "index";
    }

    @RequestMapping(value = "/attrListPage")
    public String attrListPage(){
        return "attrListPage";
    }
}

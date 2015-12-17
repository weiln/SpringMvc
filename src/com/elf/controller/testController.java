package com.elf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class testController {
	/**
	 * 跳转到用户表格页面
	 * @return
	 */
	@RequestMapping(value = "/test.htm",method = RequestMethod.GET)
    public String userList(Model model) {
		//int i=1/0;
        return "test";
    }
}

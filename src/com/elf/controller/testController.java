package com.elf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.elf.model.User;
import com.elf.model.UserResultMessage;
import com.elf.soap.soapmap.client.SoapMapClient;
import com.elf.soap.soapmap.exception.SoapException;

@Controller
public class testController {
	@Autowired
	private SoapMapClient soapMapClient;
	/**
	 * 跳转到用户表格页面
	 * @return
	 */
	@RequestMapping(value = "/test.htm",method = RequestMethod.GET)
    public String userList(Model model) {
		//int i=1/0;
        return "test";
    }
	
	/**
	 * 测试参照ibaits模式发送Soap请求的功能
	 * @return
	 */
	@RequestMapping(value = "/testSoap.htm",method = RequestMethod.GET)
	public String testSoap(){
		String  url = "http://8dcp1ngcndxg4gp.dunan.cn:8080/SpringMvc/webservice/UserService";
		String param ="admin";
		try {
			UserResultMessage o = (UserResultMessage)soapMapClient.execute(url,"user.findUserByName",param);
			System.out.println(o);
		} catch (SoapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return "test";
	}

	
	
}

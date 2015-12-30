package com.elf.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.elf.model.User;
import com.elf.model.UserResultMessage;
import com.elf.soap.soapmap.client.SoapMapClient;
import com.elf.soap.soapmap.exception.SoapException;
import com.elf.util.I18nUtil;

@Controller
public class testController {
	@Resource
	private SoapMapClient soapMapClient;
	
	@Resource
	private MessageSource messageSource;
	/**
	 * 测试
	 * @return
	 */
	@RequestMapping(value = "/test.htm",method = RequestMethod.GET)
    public String userList(Model model) {
		//int i=1/0;
		//System.out.println(I18nUtil.getTextValue("hello"));
		System.out.println(messageSource.getMessage("hello", null,null));
        return "test";
    }
	
	/**
	 * 测试参照ibaits模式发送Soap请求的功能
	 * 测试缓存
	 * @return
	 */
	@RequestMapping(value = "/testSoap.htm",method = RequestMethod.GET)
	@Cacheable(value="myCache") 
	public String testSoap(){
		String  url = "http://8dcp1ngcndxg4gp.dunan.cn:8080/SpringMvc/webservice/userService-ws";
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

package com.elf.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.elf.model.User;
import com.elf.model.UserResultMessage;
import com.elf.service.testService;
import com.elf.service.messageService.MessageService;
import com.elf.soap.soapmap.client.SoapMapClient;
import com.elf.soap.soapmap.exception.SoapException;
import com.elf.util.I18nUtil;

@Controller
public class testController {
	@Resource
	private SoapMapClient soapMapClient;
	
	@Resource
	private MessageService messageService;
	
	@Resource
	private testService ts;
	
	/**
	 * 测试
	 * @return
	 */
	@RequestMapping(value = "/test.htm",method = RequestMethod.GET)
    public String userList(Model model) {
		//int i=1/0;
		//System.out.println(I18nUtil.getTextValue("hello"));
		//System.out.println(messageSource.getMessage("hello", null,null));
		System.out.println(messageService.getText("hello"));
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
	
	
	/**
	 * 测试缓存注解@Cacheable
	 * @return
	 */
	@RequestMapping(value = "/testCacheable.htm",method = RequestMethod.GET)
	@Cacheable(value="myCache") 
	public String testCacheable(){
		System.out.println("没进缓存");
		return "test";
	}
	
	/**
	 * 测试缓存注解@CacheEvict
	 * @return
	 */
	@RequestMapping(value = "/testCacheEvict.htm",method = RequestMethod.GET)
	@CacheEvict(value="myCache") 
	public String testCacheEvict(){
		System.out.println("清除缓存");
		return "test";
	}
	
	/**
	 * 测试缓存注解@CachePut
	 * @return
	 */
	@RequestMapping(value = "/testCachePut.htm",method = RequestMethod.GET)
	@CachePut(value="myCache") 
	public String testCachePut(){
		System.out.println("测试CachePut");
		return "test";
	}
	
	@RequestMapping(value = "/testMAV.htm",method = RequestMethod.GET)
	public ModelAndView testMAV(){
		ModelAndView mav =new ModelAndView("test");
		mav.addObject("testid","testId111");
		return mav;
	}
	
	@RequestMapping(value = "/testdb.htm",method = RequestMethod.GET)
	public String testdb(){
		ts.getUsernameById(10);
		return "test";
	}
	
}

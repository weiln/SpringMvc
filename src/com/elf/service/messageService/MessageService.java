package com.elf.service.messageService;

import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
	
	@Resource
	private MessageSource messageSource;
	
	public String getText(String key){
		return messageSource.getMessage(key, null,null);
	}
	
	public String getText(String key,Object[] param){
		return messageSource.getMessage(key, param, null);
	}
	
	
	public String getText(String key,Object[] param,Locale locale){
		return messageSource.getMessage(key, param, locale);
	}
}

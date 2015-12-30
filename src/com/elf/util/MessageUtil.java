package com.elf.util;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

public class MessageUtil  {
	@Resource
	private static MessageSource messageSource;
	
    public static String getTextValue(String key) {  
        return messageSource.getMessage(key, null, null);  
    }  

}

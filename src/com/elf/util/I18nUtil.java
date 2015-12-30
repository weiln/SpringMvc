package com.elf.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class I18nUtil  implements ApplicationContextAware{
	private static ApplicationContext applicationContext;  
	  
    public static ApplicationContext getApplicationContext() {  
        return applicationContext;  
    }  
  
    @Override  
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {  
        applicationContext = arg0;  
    }  
  
    public static Object getBean(String id) {  
        Object object = null;  
        object = applicationContext.getBean(id);  
        return object;  
    }  
    
    public static String getTextValue(String key) {  
        return getApplicationContext().getMessage(key, null, null);  
    }  

}

package com.elf.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.elf.dao2.UserMapper2;

@Service
public class testService {
	
	@Resource
	private UserMapper2 userMapper2;
	
	public String getUsernameById(int id){  
        System.out.println("数据库2中查到此用户号[" + id + "]对应的用户名为[" + userMapper2.getUsernameById(id) + "]");  
        return userMapper2.getUsernameById(id);  
    }
}

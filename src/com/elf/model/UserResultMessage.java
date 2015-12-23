package com.elf.model;

import org.dom4j.Element;
import org.dom4j.VisitorSupport;

public class UserResultMessage extends VisitorSupport {
	
	private int id;
	private String username;
	private String password;
	private String email;
	
	public void visit(Element node) {
		String name = node.getName();
		String text = node.getText();

		if ("username".equals(name)) {
			username = text;
		} else if ("password".equals(name)) {
			password = text;
		} else if ("email".equals(name)) {
			email = text;
		} 
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

}

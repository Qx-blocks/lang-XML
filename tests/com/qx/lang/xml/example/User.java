package com.qx.lang.xml.example;


import com.qx.back.lang.xml.annotation.XML_GetAttribute;
import com.qx.back.lang.xml.annotation.XML_GetElement;
import com.qx.back.lang.xml.annotation.XML_SetAttribute;
import com.qx.back.lang.xml.annotation.XML_SetElement;
import com.qx.back.lang.xml.annotation.XML_Type;

@XML_Type(name="user", sub={})
public class User extends TestClass3 {

	private String name = "none";
	
	private String password;

	private String note;
	
	public String getName() {
		return name;
	}

	@XML_SetAttribute(name="name")
	public void setName(String name) {
		this.name = name;
	}


	@XML_SetAttribute(name="password")
	public void setPassword(String password) {
		this.password = password;
	}
	
	@XML_GetAttribute(name="password")
	public String getPassword() {
		return password;
	}
	
	@XML_SetElement(name="note")
	public void setNote(String note){
		this.note = note;
	}
	
	@XML_GetElement(name="note")
	public String getNote(){
		return note;
	}
}

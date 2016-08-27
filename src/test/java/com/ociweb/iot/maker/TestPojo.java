package com.ociweb.iot.maker;

import java.io.Serializable;


public class TestPojo implements Serializable {
	 
	 private static final long serialVersionUID = 1L;
	 
	 private String name;
	 public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	private int age;
	 
	 public TestPojo(){
	 }
	 
	 public TestPojo(String name, int age) {
		 this.name = name;
		 this.age = age;
		 
	 }
	 
	 @Override
	 public boolean equals(Object that) {
		 if (that instanceof TestPojo) {				 
			 return this.name.equals(((TestPojo)that).name) && this.age==(((TestPojo)that).age);
		 }
		 return false;
	 }
	 
}

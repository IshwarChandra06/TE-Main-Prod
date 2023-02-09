package com.eikona.tech.dto;

public class TransactionDto {
	
	String plant;
	
	long loginEmployee;
	
	
	public String getPlant() {
		return plant;
	}
	public void setPlant(String plant) {
		this.plant = plant;
	}
	public long getLoginEmployee() {
		return loginEmployee;
	}
	public void setLoginEmployee(long loginEmployee) {
		this.loginEmployee = loginEmployee;
	}
	
	
	public TransactionDto() {
		super();
	}
	public TransactionDto(String plant, long loginEmployee) {
		super();
		this.plant = plant;
		this.loginEmployee = loginEmployee;
	}
	
}

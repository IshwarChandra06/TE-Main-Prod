package com.eikona.tech.dto;

public class DeviceDto {
	
	private String device;
	private String serialNo;
	private String plant;
	private String building;
	private long tatalPerson;
	private long totalTransaction;
	private long totalUnregisterTransaction;
	private long capacity;
	
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public long getTatalPerson() {
		return tatalPerson;
	}
	public void setTatalPerson(long tatalPerson) {
		this.tatalPerson = tatalPerson;
	}
	public long getTotalTransaction() {
		return totalTransaction;
	}
	public void setTotalTransaction(long totalTransaction) {
		this.totalTransaction = totalTransaction;
	}
	public long getTotalUnregisterTransaction() {
		return totalUnregisterTransaction;
	}
	public void setTotalUnregisterTransaction(long totalUnregisterTransaction) {
		this.totalUnregisterTransaction = totalUnregisterTransaction;
	}
	public long getCapacity() {
		return capacity;
	}
	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}
	public String getPlant() {
		return plant;
	}
	public void setPlant(String plant) {
		this.plant = plant;
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		this.building = building;
	}
	

}

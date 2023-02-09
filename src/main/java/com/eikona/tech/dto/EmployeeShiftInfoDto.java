package com.eikona.tech.dto;

import java.util.Date;

public class EmployeeShiftInfoDto {
	
	private Date lastModifiedDate;
	
	private String employeeId;
	
	private String shiftName;

	private Date shiftStartTime;
	
	private Date shiftEndTime;

	private boolean isHoliday;
	
	private Date date;
	
	private String dayNo;

	private String dayModel;

	private String workScheduleExternalCode;
	

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getShiftName() {
		return shiftName;
	}

	public void setShiftName(String shiftName) {
		this.shiftName = shiftName;
	}

	public Date getShiftStartTime() {
		return shiftStartTime;
	}

	public void setShiftStartTime(Date shiftStartTime) {
		this.shiftStartTime = shiftStartTime;
	}
	

	public Date getShiftEndTime() {
		return shiftEndTime;
	}

	public void setShiftEndTime(Date shiftEndTime) {
		this.shiftEndTime = shiftEndTime;
	}

	public boolean isHoliday() {
		return isHoliday;
	}

	public void setHoliday(boolean isHoliday) {
		this.isHoliday = isHoliday;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDayNo() {
		return dayNo;
	}

	public void setDayNo(String dayNo) {
		this.dayNo = dayNo;
	}

	public String getDayModel() {
		return dayModel;
	}

	public void setDayModel(String dayModel) {
		this.dayModel = dayModel;
	}

	public String getWorkScheduleExternalCode() {
		return workScheduleExternalCode;
	}

	public void setWorkScheduleExternalCode(String workScheduleExternalCode) {
		this.workScheduleExternalCode = workScheduleExternalCode;
	}

}

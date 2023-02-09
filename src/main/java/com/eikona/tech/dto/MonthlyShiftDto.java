package com.eikona.tech.dto;

import java.util.List;

public class MonthlyShiftDto {
	
	private String empId;
	private String firstName;
	private String lastName;
	private String grade;
	private String department;
	private String designation;
	private String company;
	private String totalPresentCount;
	private String totalAbsentCount;
	private String mobile;
	private String totalOverTime;
	private String totalDays;

	private List<String> dateList;

	public String getEmpId() {
		return empId;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDepartment() {
		return department;
	}

	public String getDesignation() {
		return designation;
	}

	public String getTotalPresentCount() {
		return totalPresentCount;
	}

	public String getTotalAbsentCount() {
		return totalAbsentCount;
	}



	public String getTotalOverTime() {
		return totalOverTime;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getTotalDays() {
		return totalDays;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public void setTotalPresentCount(String totalPresentCount) {
		this.totalPresentCount = totalPresentCount;
	}

	public void setTotalAbsentCount(String totalAbsentCount) {
		this.totalAbsentCount = totalAbsentCount;
	}
	public void setTotalOverTime(String totalOverTime) {
		this.totalOverTime = totalOverTime;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public void setTotalDays(String totalDays) {
		this.totalDays = totalDays;
	}

	public List<String> getDateList() {
		return dateList;
	}

	public void setDateList(List<String> dateList) {
		this.dateList = dateList;
	}
	
}

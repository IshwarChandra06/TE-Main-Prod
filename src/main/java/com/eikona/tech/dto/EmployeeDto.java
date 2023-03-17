package com.eikona.tech.dto;

import java.util.Date;

import javax.persistence.Column;

public class EmployeeDto {

	private Long id;

	private Date lastModifiedDate;

	private String firstName;

	private String lastName;

	private String employeeId;

	private String department;

	private String function;

	private Date joinDate;

	private Date endDate;

	private String cadre;

	private String payGrade;
	
	private String managerId;
	
	private String managerName;
	
	private String building;
	
	private String accessLevel;
	
	private String userState;
	
	private String lanyardColor;
	
	private String cardId;
	
	private Date cardIssueDate;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
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

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public Date getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getCadre() {
		return cadre;
	}

	public void setCadre(String cadre) {
		this.cadre = cadre;
	}

	public String getPayGrade() {
		return payGrade;
	}

	public void setPayGrade(String payGrade) {
		this.payGrade = payGrade;
	}

	public String getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(String accessLevel) {
		this.accessLevel = accessLevel;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public String getManagerId() {
		return managerId;
	}

	public void setManagerId(String managerId) {
		this.managerId = managerId;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public String getUserState() {
		return userState;
	}

	public void setUserState(String userState) {
		this.userState = userState;
	}

	public String getLanyardColor() {
		return lanyardColor;
	}

	public void setLanyardColor(String lanyardColor) {
		this.lanyardColor = lanyardColor;
	}

	public Date getCardIssueDate() {
		return cardIssueDate;
	}

	public void setCardIssueDate(Date cardIssueDate) {
		this.cardIssueDate = cardIssueDate;
	}

	
	
	

}

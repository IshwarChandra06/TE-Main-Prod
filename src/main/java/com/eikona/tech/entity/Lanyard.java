package com.eikona.tech.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

@Entity(name = "te_lanyard_management")
public class Lanyard extends Auditable<String> implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "issue_date_str")
	private String issueDateStr;
	
	@Column(name = "issue_date")
	private Date issueDate;
	
	@ManyToOne
	@JoinColumn(name="type_id")
	private LanyardType type;
	
	@ManyToOne
	@JoinColumn(name="employee_id")
	private Employee employee;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "reason")
	private String reason;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIssueDateStr() {
		return issueDateStr;
	}

	public void setIssueDateStr(String issueDateStr) {
		this.issueDateStr = issueDateStr;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public LanyardType getType() {
		return type;
	}

	public void setType(LanyardType type) {
		this.type = type;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	


	
	
}

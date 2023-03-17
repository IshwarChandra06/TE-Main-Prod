package com.eikona.tech.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity(name = "te_employee_master")
public class Employee extends Auditable<String> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "employee_id")
	private String employeeId;

	@Column(name = "department")
	private String department;

	@Column(name = "designation")
	private String designation;
	
	@Column(name = "contact_no")
	private String contactNo;
	
	@Column(name = "email_id")
	private String emailId;
	
	@ManyToOne
	@JoinColumn(name = "employee_type_id")
	private EmployeeType employeeType;

	@Column(name = "join_date")
	private Date joinDate;

	@Column(name = "end_date")
	private Date endDate;
	
	@Column
	private String joinDateStr;

	@Column
	private String endDateStr;

	@Column(name = "cadre")
	private String cadre;

	@Column(name = "pay_grade")
	private String payGrade;
	
	@Column(name = "card_id")
	private String cardId;
	
	@Column(name = "card_issue_date")
	private Date cardIssueDate;
	
	@Column(name = "manager_id")
	private String managerId;
	
	@Column(name = "manager_name")
	private String managerName;
	
	@Column(name = "manager_email")
	private String managerEmail;
	
	@Column(name = "hostel_name")
	private String hostelName;
	
	@Column(name = "hostel_warden_name")
	private String hostelWardenName;
	
	@Column(name = "hostel_warden_email")
	private String hostelWardenEmail;
	
	@Column(name = "hostel_warden_mobile")
	private String hostelWardenMobile;
	
	@Column(name = "bus_no")
	private String busNo;
	
	@Column(name = "nodal_point")
	private String nodalPoint;
	
	@Column(name = "eeto_name")
	private String eetoName;
	
	@Column
	private String palntStr;

	@Column
	private String buildingStr;

	@Column
	private String accessLevelStr;
	
	@ManyToMany
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinTable(name = "te_employee_metalexception",
    joinColumns = @JoinColumn(name = "employee_id"),
    inverseJoinColumns = @JoinColumn(name = "metal_exception_id"),
    indexes = {
	        @Index(name = "idx_accesslevel_id", columnList = "metal_exception_id"),
	        @Index(name = "idx_employee_id", columnList = "employee_id")
	}
	    )
	private List<MetalException> metalExceptions;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "lanyard_color")
	private String lanyardColor;
	
	@ManyToMany
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinTable(name = "te_employee_accesslevel",
    joinColumns = @JoinColumn(name = "employee_id"),
    inverseJoinColumns = @JoinColumn(name = "accesslevel_id"),
    indexes = {
	        @Index(name = "idx_accesslevel_id", columnList = "accesslevel_id"),
	        @Index(name = "idx_employee_id", columnList = "employee_id")
	}
	    )
	private List<AccessLevel> accessLevel;
	
	@Column(name = "crop_image")
	private byte[] cropImage;
	
	@Column(name = "source")
	private String source;
	
	
	@Column(name = "is_deleted")
    private boolean isDeleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
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

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public List<AccessLevel> getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(List<AccessLevel> accessLevel) {
		this.accessLevel = accessLevel;
	}

	public String getAccesslevels() {
		
		List<AccessLevel> acclevelList=getAccessLevel();
		StringJoiner strJoiner= new StringJoiner(",");
		for(AccessLevel acclevel:acclevelList) {
			strJoiner.add(acclevel.getName());
		}
		return strJoiner.toString();
	}

   public String getAccesslevelIds() {
		
		List<AccessLevel> acclevelList=getAccessLevel();
		StringJoiner strJoiner= new StringJoiner(",");
		for(AccessLevel acclevel:acclevelList) {
			 strJoiner.add(acclevel.getAccessId());
		}
		return strJoiner.toString();
	}

public String getManagerName() {
	return managerName;
}

public void setManagerName(String managerName) {
	this.managerName = managerName;
}

public String getManagerEmail() {
	return managerEmail;
}

public void setManagerEmail(String managerEmail) {
	this.managerEmail = managerEmail;
}

public String getHostelName() {
	return hostelName;
}

public void setHostelName(String hostelName) {
	this.hostelName = hostelName;
}

public String getHostelWardenName() {
	return hostelWardenName;
}

public void setHostelWardenName(String hostelWardenName) {
	this.hostelWardenName = hostelWardenName;
}

public String getHostelWardenEmail() {
	return hostelWardenEmail;
}

public void setHostelWardenEmail(String hostelWardenEmail) {
	this.hostelWardenEmail = hostelWardenEmail;
}

public String getHostelWardenMobile() {
	return hostelWardenMobile;
}

public void setHostelWardenMobile(String hostelWardenMobile) {
	this.hostelWardenMobile = hostelWardenMobile;
}

public String getBusNo() {
	return busNo;
}

public void setBusNo(String busNo) {
	this.busNo = busNo;
}

public String getNodalPoint() {
	return nodalPoint;
}

public void setNodalPoint(String nodalPoint) {
	this.nodalPoint = nodalPoint;
}

public String getEetoName() {
	return eetoName;
}

public void setEetoName(String eetoName) {
	this.eetoName = eetoName;
}

public String getManagerId() {
	return managerId;
}

public void setManagerId(String managerId) {
	this.managerId = managerId;
}

public String getContactNo() {
	return contactNo;
}

public void setContactNo(String contactNo) {
	this.contactNo = contactNo;
}

public String getEmailId() {
	return emailId;
}

public void setEmailId(String emailId) {
	this.emailId = emailId;
}

public EmployeeType getEmployeeType() {
	return employeeType;
}

public void setEmployeeType(EmployeeType employeeType) {
	this.employeeType = employeeType;
}


public byte[] getCropImage() {
	return cropImage;
}

public void setCropImage(byte[] cropImage) {
	this.cropImage = cropImage;
}

public String getStatus() {
	return status;
}

public void setStatus(String status) {
	this.status = status;
}

public String getLanyardColor() {
	return lanyardColor;
}

public void setLanyardColor(String lanyardColor) {
	this.lanyardColor = lanyardColor;
}

public List<MetalException> getMetalExceptions() {
	return metalExceptions;
}

public void setMetalExceptions(List<MetalException> metalExceptions) {
	this.metalExceptions = metalExceptions;
}
	
public String getMetalExceptionName() {
	
	List<MetalException> metalExceptionList=getMetalExceptions();
	StringJoiner strJoiner= new StringJoiner(",");
	for(MetalException metalException:metalExceptionList) {
		strJoiner.add(metalException.getName());
	}
	return strJoiner.toString();
}
public String getPalntStr() {

	List<AccessLevel> accessLevelList = getAccessLevel();
	List<String> plantList = new ArrayList<>();
	StringJoiner plantName = new StringJoiner(",");

	for (AccessLevel accessLevel : accessLevelList) {

		if (null != accessLevel.getBuilding()) {
			if (null != accessLevel.getBuilding().getPlant())
				if(!plantList.contains(accessLevel.getBuilding().getPlant().getName())) {
					plantList.add(accessLevel.getBuilding().getPlant().getName());
					plantName.add(accessLevel.getBuilding().getPlant().getName());
				}
			
		}
	}

	return plantName.toString();
}

public void setPalntStr(String palntStr) {
	this.palntStr = palntStr;
}

public String getBuildingStr() {

	List<AccessLevel> accessLevelList = getAccessLevel();

	List<String> buildingList = new ArrayList<>();

	StringJoiner buildingName = new StringJoiner(",");

	for (AccessLevel accessLevel : accessLevelList) {

		if (null != accessLevel.getBuilding()) {
			if (!buildingList.contains(accessLevel.getBuilding().getName())) {
				buildingList.add(accessLevel.getBuilding().getName());
				buildingName.add(accessLevel.getBuilding().getName());
			}

		}

	}
	return buildingName.toString();
}

public void setBuildingStr(String buildingStr) {
	this.buildingStr = buildingStr;
}

public String getAccessLevelStr() {
	List<AccessLevel> accessLevelList = getAccessLevel();

	List<String> accessList = new ArrayList<>();

	StringJoiner accessName = new StringJoiner(",");

	for (AccessLevel accessLevel : accessLevelList) {

		if (!accessList.contains(accessLevel.getName())) {
			accessList.add(accessLevel.getName());
			accessName.add(accessLevel.getName());
		}
	}
	return accessName.toString();
}

public void setAccessLevelStr(String accessLevelStr) {
	this.accessLevelStr = accessLevelStr;
}

public String getJoinDateStr() {
	return joinDateStr;
}

public void setJoinDateStr(String joinDateStr) {
	this.joinDateStr = joinDateStr;
}

public String getEndDateStr() {
	return endDateStr;
}

public void setEndDateStr(String endDateStr) {
	this.endDateStr = endDateStr;
}

public Date getCardIssueDate() {
	return cardIssueDate;
}

public void setCardIssueDate(Date cardIssueDate) {
	this.cardIssueDate = cardIssueDate;
}

public String getSource() {
	return source;
}

public void setSource(String source) {
	this.source = source;
}


}

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

@Entity(name = "te_employee_shift_info")
//@Table(indexes = {
//		  @Index(name = "mulitIndexEmployeeShift", columnList = "shift,date_str"),
//		})
public class EmployeeShiftInfo extends Auditable<String> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
    @ManyToOne
	@JoinColumn(name="employee_id")
	private Employee employee;
	
	@Column
	private String shift;
	
	@Column
	private Date startTime;
	
	@Column
	private Date endTime;
	

	@Column(name = "date")
	private Date date;
	
	@Column(name = "date_str")
	private String dateStr;

	@Column(name = "is_holiday")
	private boolean isHoliday;

	@Column(name = "day")
	private String day;

	@Column(name = "day_model")
	private String dayModel;

	@Column(name = "work_schedule_external_code")
	private String workScheduleExternalCode;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getShift() {
		return shift;
	}

	public void setShift(String shift) {
		this.shift = shift;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isHoliday() {
		return isHoliday;
	}

	public void setHoliday(boolean isHoliday) {
		this.isHoliday = isHoliday;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
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

	public String getDateStr() {
		return dateStr;
	}

	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}
	
	

}

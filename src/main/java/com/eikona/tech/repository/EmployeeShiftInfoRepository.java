package com.eikona.tech.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.EmployeeShiftInfo;
@Repository
public interface EmployeeShiftInfoRepository  extends DataTablesRepository<EmployeeShiftInfo, Long> {
	
    @Query("select es from com.eikona.tech.entity.EmployeeShiftInfo es where es.employee.employeeId=:employeeId and es.dateStr=:dateStr")
	EmployeeShiftInfo findByEmployeeIdAndDateStrCustom(String employeeId, String dateStr);
    
	List<EmployeeShiftInfo> findByShiftAndDateStr(String shift, String date);
	
	@Query("select es from com.eikona.tech.entity.EmployeeShiftInfo es where es.date>=:startDate and es.date<=:endDate and es.employee is not null and es.employee.managerEmail=:managerEmail")
	List<EmployeeShiftInfo> findByNextSevenDaysShiftInfoCustom(Date startDate, Date endDate, String managerEmail);

	@Query("select es from com.eikona.tech.entity.EmployeeShiftInfo es where es.date>=:startDate and es.date<=:endDate and es.employee is not null and es.employee.hostelWardenEmail=:wardenEmail")
	List<EmployeeShiftInfo> findByNextSevenDaysShiftInfoHostelCustom(Date startDate, Date endDate,String wardenEmail);
	
	@Query("select es from com.eikona.tech.entity.EmployeeShiftInfo es where es.date>=:startDate and es.date<=:endDate and es.employee is not null "
			+ "and es.employee.employeeId=:employeeId")
	List<EmployeeShiftInfo> findDetailsByDateCustom(String employeeId, Date startDate, Date endDate);

	@Query("select es from com.eikona.tech.entity.EmployeeShiftInfo es where es.date>=:startDate and es.date<=:endDate and es.employee is not null")
	List<EmployeeShiftInfo> findByDateCustom(Date startDate, Date endDate);




}

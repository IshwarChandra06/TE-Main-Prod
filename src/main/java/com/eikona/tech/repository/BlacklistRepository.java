package com.eikona.tech.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.Blacklist;
import com.eikona.tech.entity.Employee;

@Repository
public interface BlacklistRepository extends DataTablesRepository<Blacklist, Long>{

	List<Blacklist> findByEmployee(Employee employee);

	List<Blacklist> findByEmployeeAndIsRemoveFalse(Employee emp);
	
	@Query("select e from com.eikona.tech.entity.Blacklist e where e.startDate>=:date and e.endDate<=:date and e.employee.employeeId=:empId")
	Blacklist findByEmployeeAndDateCustom(String empId, Date date);

	@Query("select count(e) from com.eikona.tech.entity.Blacklist e where ((e.startDate>=:startDate and e.startDate<=:endDate) or (e.endDate>=:startDate and e.endDate<=:endDate)) and e.employee.employeeId=:employeeId and e.status='Suspended'")
	Long findByDateAndEmpIdCustom(String employeeId, Date startDate, Date endDate);

	@Query("select e from com.eikona.tech.entity.Blacklist e where e.createdDate>=:sDate and e.createdDate<=:eDate and e.status =:status")
	List<Blacklist> findByCreatedDateAndStatusCustom(Date sDate, Date eDate, String status);

}

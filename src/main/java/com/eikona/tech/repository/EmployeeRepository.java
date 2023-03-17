package com.eikona.tech.repository;


import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.Employee;


@Repository
public interface EmployeeRepository extends DataTablesRepository<Employee, Long> {

	public List<Employee> findAllByIsDeletedFalse();

	@Query("select e from com.eikona.tech.entity.Employee e where e.department like 'HR%' or e.department like 'IT%'")
	List<Employee> getHundredEmployee();
	
	@Query("select e from com.eikona.tech.entity.Employee e where NOT(e.accessLevel IS EMPTY)")
	Page<Employee> getAllByLimit(Pageable pageable);

    Employee findByEmployeeId(String empId);
    
    @Query("select e from com.eikona.tech.entity.Employee e where e.lastModifiedDate>=:lastSyncDate and e.lastModifiedDate<=:currentDate and e.isDeleted=false")
	List<Employee> findByLastModifiedDateCustom(Date lastSyncDate, Date currentDate);
    
    @Query("select e from com.eikona.tech.entity.Employee e where e.lastModifiedDate>=:lastSyncDate and e.lastModifiedDate<=:currentDate and e.isDeleted=false")
    Page<Employee> findByLastModifiedDateCustom(Date lastSyncDate, Date currentDate,Pageable pageable);

    @Query("select distinct(e.managerEmail) from com.eikona.tech.entity.Employee e where e.managerEmail is not null")
    List<String> findDistinctByManagerEmailCustom();

    @Query("select distinct(e.hostelWardenEmail) from com.eikona.tech.entity.Employee e where e.hostelWardenEmail is not null")
    List<String> findDistinctByHostelWardenEmailCustom();

	public Employee findByEmployeeIdAndIsDeletedFalse(String empId);

	public List<Employee> findAllByStatus(String string);
	
	@Query("select emp.employeeId from com.eikona.tech.entity.Employee as emp where emp.isDeleted=false")
	List<String> getEmpIdAndIsDeletedFalseCustom();

	 




}
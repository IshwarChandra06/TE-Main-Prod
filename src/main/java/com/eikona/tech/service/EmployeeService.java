package com.eikona.tech.service;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.dto.SearchRequestDto;
import com.eikona.tech.entity.Employee;

public interface EmployeeService {

	/**
	 * Returns all employee List, which are isDeleted false.
	 * @param
	 */
	List<Employee> getAll();
	/**
	 * This function saves the employee in database according to the respective object.  
	 * @param 
	 * @return 
	 */
    Employee save(Employee employee);
    /**
	 * This function retrieves the employee from database according to the respective id.  
	 * @param
	 */
    Employee getById(long id);
    
	/**
	 * This function deletes the employee from database according to the respective id.  
	 * @param
	 */
	void deleteById(long id, Principal principal);
	
	Page<Employee> searchByField(int pageNo, int pageSize, String sortField, String sortOrder,
			SearchRequestDto paginatedDto, Principal principal);
	
	void saveEmployeeAccessLevelAssociation(Employee employee, Long id, Principal principal);
	
	String storeEmployeeAccessZoneList(MultipartFile file);
	
	String storeEmployeeMasterList(MultipartFile file);
	
	void saveEmployeeMetalException(Employee employee, Long id, Principal principal);
	
	PaginationDto<Employee> searchByField(String sDate, String eDate, String firstName, String lastName, String empId,
			String department, String designation, String employeeType, String cardNo, String lanyard, String status,
			int pageno, String sortField, String sortDir);
	
}

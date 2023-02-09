package com.eikona.tech.service;


import java.util.List;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.EmployeeType;


public interface EmployeeTypeService {
	/**
	 * Returns all employee type List, which are isDeleted false.
	 * @param
	 */
	List<EmployeeType> getAll();
	/**
	 * This function saves the employee type in database according to the respective object.  
	 * @param 
	 */
	void save(EmployeeType employeeType);
	/**
	 * This function retrieves the employee type from database according to the respective id.  
	 * @param
	 */
	EmployeeType getById(long id);
	/**
	 * This function deletes the employee type from database according to the respective id.  
	 * @param
	 */
	void deleteById(long id);
	
	
	PaginationDto<EmployeeType> searchByField(Long id, String name, int pageno, String sortField, String sortDir);
}

package com.eikona.tech.service;


import java.util.List;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Zone;


public interface ZoneService {
	/**
	 * Returns all zone List, which are isDeleted false.
	 * @param
	 */
	List<Zone> getAll();
	/**
	 * This function saves the zone in database according to the respective object.  
	 * @param 
	 */
	void save(Zone employeeType);
	/**
	 * This function retrieves the zone from database according to the respective id.  
	 * @param
	 */
	Zone getById(long id);
	/**
	 * This function deletes the zone from database according to the respective id.  
	 * @param
	 */
	void deleteById(long id);
	
	
	PaginationDto<Zone> searchByField(Long id, String name, int pageno, String sortField, String sortDir);
}

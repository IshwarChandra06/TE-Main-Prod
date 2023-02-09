package com.eikona.tech.service;


import java.util.List;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.LanyardType;


public interface LanyardTypeService {
	/**
	 * Returns all lanyard type List, which are isDeleted false.
	 * @param
	 */
	List<LanyardType> getAll();
	/**
	 * This function saves the lanyard type in database according to the respective object.  
	 * @param 
	 */
	void save(LanyardType lanyardType);
	/**
	 * This function retrieves the lanyard type from database according to the respective id.  
	 * @param
	 */
	LanyardType getById(long id);
	/**
	 * This function deletes the lanyard type from database according to the respective id.  
	 * @param
	 */
	void deleteById(long id);
	
	
	PaginationDto<LanyardType> searchByField(Long id, String name, int pageno, String sortField, String sortDir);
}

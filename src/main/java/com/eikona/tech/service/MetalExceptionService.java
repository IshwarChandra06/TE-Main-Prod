package com.eikona.tech.service;


import java.util.List;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.MetalException;


public interface MetalExceptionService {
	/**
	 * Returns all metal exception type List, which are isDeleted false.
	 * @param
	 */
	List<MetalException> getAll();
	/**
	 * This function saves the metal exception type in database according to the respective object.  
	 * @param 
	 */
	void save(MetalException metalexception);
	/**
	 * This function retrieves the metal exception type from database according to the respective id.  
	 * @param
	 */
	MetalException getById(long id);
	/**
	 * This function deletes the metal exception type from database according to the respective id.  
	 * @param
	 */
	void deleteById(long id);
	
	
	PaginationDto<MetalException> searchByField(Long id, String name, int pageno, String sortField, String sortDir);
}

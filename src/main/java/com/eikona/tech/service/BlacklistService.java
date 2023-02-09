package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Blacklist;

public interface BlacklistService {
	
	Blacklist save(Blacklist blacklist);

	PaginationDto<Blacklist> searchByField(String empId, String orderBy,
			int pageno, String sortField, String sortDir);

	PaginationDto<Blacklist> searchByField(String employeeId, int pageno, String sortField, String sortDir);

	PaginationDto<Blacklist> searchByField(String employeeId, String status, String orderBy, int pageno,
			String sortField, String sortDir);

	

}

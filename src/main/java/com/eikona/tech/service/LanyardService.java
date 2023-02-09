package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Lanyard;

public interface LanyardService {
	Lanyard save(Lanyard lanyard);

	Lanyard getById(Long id);
	
	PaginationDto<Lanyard> searchByFieldView(String employeeId, int pageno, String sortField, String sortDir);

	PaginationDto<Lanyard> searchByField(String sDate, String eDate, String employeeId, String type, String status,
			int pageno, String sortField, String sortDir);
}

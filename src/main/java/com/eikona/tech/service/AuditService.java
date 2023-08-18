package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Audit;

public interface AuditService {
	
	PaginationDto<Audit> searchByField(String sDate, String eDate, String empId,
			String firstName,String lastName,String activity, int pageno, String sortField, String sortDir);
}

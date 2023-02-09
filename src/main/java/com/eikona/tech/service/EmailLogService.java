package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.EmailLogs;

public interface EmailLogService {
	
	PaginationDto<EmailLogs> searchByField(String sDate, String eDate, String mailId, String type, int pageno, String sortField, String sortDir);
}

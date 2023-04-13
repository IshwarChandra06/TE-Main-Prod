package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Transaction;

public interface TransactionService {
	PaginationDto<Transaction> searchByField(String sDate, String eDate, String employeeId,
			String employeeName, String employeeType, String department, String designation, String device, int pageno,String sortField, String sortDir);
}

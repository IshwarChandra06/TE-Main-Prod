package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.DailyReport;

public interface WorkStatusReportService {

	public PaginationDto<DailyReport> searchByField(String sDate, String eDate, String employeeId, String employeeName,
			String department, String designation, String employeeType, String workHour, int pageno, String sortField,
			String sortDir);

}

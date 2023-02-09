package com.eikona.tech.service;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.dto.SearchRequestDto;
import com.eikona.tech.entity.EmployeeShiftInfo;

public interface EmployeeShiftInfoService {

	Page<EmployeeShiftInfo> searchByField(int pageNo, int pageSize, String sortField, String sortOrder,
			SearchRequestDto paginatedDto, Principal principal);

	List<EmployeeShiftInfo> findAll();

	PaginationDto<EmployeeShiftInfo> searchByField(String sDate, String eDate, String employeeId,
			String employeeName, String department, String shift, int pageno, String sortField, String sortDir);

	PaginationDto<EmployeeShiftInfo> searchByField(String date, String employeeId, String employeeName,
			String department, String shift, int pageno, String sortField, String sortDir);


}

package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Shift;

public interface ShiftService {

	PaginationDto<Shift> searchByField(Long id, String name, String sDate, String day, int pageno, String sortField,
			String sortDir);
}

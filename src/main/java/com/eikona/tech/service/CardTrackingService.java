package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.CardTracking;

public interface CardTrackingService {

	CardTracking save(CardTracking cardTracking);

	CardTracking getById(Long id);

	void deletedById(long id);


	PaginationDto<CardTracking> searchByField(String empId, int pageno, String sortField, String sortDir);

	PaginationDto<CardTracking> searchByField(String cardId, String employee, String type, String sDate,
			String eDate, int pageno, String sortField, String sortDir);
	
}

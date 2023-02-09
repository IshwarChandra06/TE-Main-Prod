package com.eikona.tech.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.constants.TransactionConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Transaction;
import com.eikona.tech.repository.TransactionRepository;
import com.eikona.tech.service.TransactionService;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class TransactionServiceImpl implements TransactionService{
	
	
	@Autowired
	private GeneralSpecificationUtil<Transaction> generalSpecification;

	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private TransactionRepository transactionRepository;



	@Override
	public PaginationDto<Transaction> searchByField(String sDate, String eDate, String employeeId,
			String employeeName, String employeeType, int pageno,String sortField, String sortDir) {
		Date startDate = null;
		Date endDate = null;
		if (!sDate.isEmpty() && !eDate.isEmpty()) {
			SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
			try {

				startDate = format.parse(sDate);
				endDate = format.parse(eDate);
				endDate = calendarUtil.getConvertedDate(endDate, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}

		Page<Transaction> page = getTransactionBySpecification(employeeId, employeeName, employeeType, pageno, sortField, sortDir, startDate, endDate);
		List<Transaction> employeeShiftList = page.getContent();

		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC : ApplicationConstants.ASC;
		PaginationDto<Transaction> dtoList = new PaginationDto<Transaction>(employeeShiftList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir,
				ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Transaction> getTransactionBySpecification(String employeeId, String employeeName,
			String employeeType,  int pageno, String sortField,String sortDir, Date startDate, Date endDate) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno -NumberConstants.ONE , NumberConstants.TEN, sort);

		Specification<Transaction> dateSpec = generalSpecification.dateSpecification(startDate, endDate,
				TransactionConstants.PUNCH_DATE);
		Specification<Transaction> empIdSpec = generalSpecification.stringSpecification(employeeId, TransactionConstants.EMP_ID);
		Specification<Transaction> empNameSpec = generalSpecification.stringSpecification(employeeName, ApplicationConstants.NAME);
		Specification<Transaction> employeeTypeSpec = generalSpecification.stringSpecification(employeeType,"employeeType");

		Page<Transaction> page = transactionRepository.findAll(dateSpec.and(empIdSpec).and(empNameSpec)
				.and(employeeTypeSpec), pageable);
		return page;
	}

	

	
}

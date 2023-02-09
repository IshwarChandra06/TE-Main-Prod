package com.eikona.tech.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.eikona.tech.constants.CardTrackingConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.CardTracking;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.repository.CardTrackingRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.service.CardTrackingService;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class CardTrackingServiceImpl implements CardTrackingService {
	
	@Autowired
	private CardTrackingRepository cardTrackingRepository;
	
	@Autowired
	private GeneralSpecificationUtil<CardTracking> generalSpecification;

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Override
	public CardTracking save(CardTracking cardTracking) {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Employee employee = cardTracking.getEmployee();
		try {
			if(!cardTracking.getDateStr().isEmpty())
			 cardTracking.setDate(dateFormat.parse(cardTracking.getDateStr()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		employee.setCardId(cardTracking.getCardId());
		if (null == cardTracking.getId()) {
			try {
				cardTracking.setIssueDate(dateFormat.parse(cardTracking.getIssueDateStr()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}else {
			CardTracking cardTrackingObj = cardTrackingRepository.findById(cardTracking.getId()).get();
			cardTracking.setIssueDateStr(cardTrackingObj.getIssueDateStr());
			cardTracking.setIssueDate(cardTrackingObj.getIssueDate());
			
			cardTracking.setCreatedBy(cardTrackingObj.getCreatedBy());
			cardTracking.setCreatedDate(cardTrackingObj.getCreatedDate());
		}
		
		if("Lost".equalsIgnoreCase(cardTracking.getType())) {
			List<AccessLevel> accessLevelList = new ArrayList<>();
			
			employee.setCardId(null);
			employee.setAccessLevel(accessLevelList);
			
			employeeRepository.save(employee);
			
		}
		if("Return".equalsIgnoreCase(cardTracking.getType())) {
			employee.setCardId(null);
			employeeRepository.save(employee);
			
		}
		
		return cardTrackingRepository.save(cardTracking);
		
	}


	@Override
	public CardTracking getById(Long id) {
		
		return cardTrackingRepository.findById(id).get();
	}

	@Override
	public void deletedById(long id) {
		
		cardTrackingRepository.deleteById(id);
	}

	@Override
	public PaginationDto<CardTracking> searchByField(String cardId, String employee, String type, String sDate,String eDate,
			int pageno, String sortField, String sortDir) {
		
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
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<CardTracking> page = getCardTrackingPage(cardId, employee, type, startDate,endDate, pageno, sortField, sortDir);
        List<CardTracking> cardTrackingList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<CardTracking> dtoList = new PaginationDto<CardTracking>(cardTrackingList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<CardTracking> getCardTrackingPage(String cardId, String employee, String type, Date startDate,Date endDate, int pageno,
			String sortField, String sortDir) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<CardTracking> cardIdSpc = generalSpecification.stringSpecification(cardId, CardTrackingConstants.CARD_ID);
		Specification<CardTracking> empSpc = generalSpecification.foreignKeyStringSpecification(employee, CardTrackingConstants.EMPLOYEE,EmployeeConstants.EMPLOYEE_ID);
		Specification<CardTracking> typeSpc = generalSpecification.stringSpecification(type, CardTrackingConstants.TYPE);
		Specification<CardTracking> dateSpec = generalSpecification.dateSpecification(startDate, endDate, ApplicationConstants.DATE);
		
    	Page<CardTracking> page = cardTrackingRepository.findAll(cardIdSpc.and(empSpc).and(typeSpc).and(dateSpec),pageable);
		return page;
	
	}


	@Override
	public PaginationDto<CardTracking> searchByField(String empId, int pageno, String sortField, String sortDir) {

		
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<CardTracking> empSpc = generalSpecification.foreignKeyStringSpecification(empId, CardTrackingConstants.EMPLOYEE, EmployeeConstants.EMPLOYEE_ID);
		
    	Page<CardTracking> page = cardTrackingRepository.findAll(empSpc,pageable);
		
        List<CardTracking> cardTrackingList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<CardTracking> dtoList = new PaginationDto<CardTracking>(cardTrackingList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	
	}
	

}

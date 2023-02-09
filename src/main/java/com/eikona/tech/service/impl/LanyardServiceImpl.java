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
import com.eikona.tech.constants.CardTrackingConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Lanyard;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.LanyardRepository;
import com.eikona.tech.service.LanyardService;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class LanyardServiceImpl implements LanyardService {

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private LanyardRepository lanyardRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Lanyard> generalSpecification;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Override
	public Lanyard save(Lanyard lanyard) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Employee employee = lanyard.getEmployee();
			
			   if(null!=lanyard.getType())
			    employee.setLanyardColor(lanyard.getType().getName());
				if((!lanyard.getIssueDateStr().isEmpty())) {
						lanyard.setIssueDate(dateFormat.parse(lanyard.getIssueDateStr()));
					} 
			employeeRepository.save(employee);
		}catch (Exception e) {
		e.printStackTrace();
		}
		return lanyardRepository.save(lanyard);
	}

	@Override
	public Lanyard getById(Long id) {
		return lanyardRepository.findById(id).get();
	}

	@Override
	public PaginationDto<Lanyard> searchByField(String sDate, String eDate, String employeeId, String type, String status,
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
		Page<Lanyard> page = getLanyardPage(startDate,endDate,employeeId, type, status, pageno, sortField, sortDir);
        List<Lanyard> lanyardList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Lanyard> dtoList = new PaginationDto<Lanyard>(lanyardList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}
	
	private Page<Lanyard> getLanyardPage( Date startDate, Date endDate, String employeeId, String type, String status, int pageno,String sortField, String sortDir) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Lanyard> dateSpec = generalSpecification.dateSpecification(startDate, endDate, "issueDate");
		Specification<Lanyard> empSpc = generalSpecification.foreignKeyStringSpecification(employeeId, CardTrackingConstants.EMPLOYEE,EmployeeConstants.EMPLOYEE_ID);
		Specification<Lanyard> typeSpc = generalSpecification.foreignKeyStringSpecification(type, "type","name");
		Specification<Lanyard> replaceTypeSpc = generalSpecification.stringSpecification(status, "status");
		
    	Page<Lanyard> page = lanyardRepository.findAll(empSpc.and(typeSpc).and(dateSpec).and(replaceTypeSpc),pageable);
		return page;
	
	}

	@Override
	public PaginationDto<Lanyard> searchByFieldView(String employeeId, int pageno, String sortField, String sortDir) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<Lanyard> empSpc = generalSpecification.foreignKeyStringSpecification(employeeId, CardTrackingConstants.EMPLOYEE, EmployeeConstants.EMPLOYEE_ID);
		Page<Lanyard> page = lanyardRepository.findAll(empSpc,pageable);
        List<Lanyard> lanyardList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Lanyard> dtoList = new PaginationDto<Lanyard>(lanyardList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

}

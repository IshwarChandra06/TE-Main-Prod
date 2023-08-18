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
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Audit;
import com.eikona.tech.repository.AuditRepository;
import com.eikona.tech.service.AuditService;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class AuditServiceImpl implements AuditService{
	
	@Autowired
	private GeneralSpecificationUtil<Audit> generalSpecificationUtil;
	
	@Autowired
	private AuditRepository auditRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;

	@Override
	public PaginationDto<Audit> searchByField(String sDate, String eDate, String empId, String firstName,
			String lastName, String activity, int pageno, String sortField, String sortDir) {
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
		
		Page<Audit> page = getAuditPage(startDate,endDate, empId, firstName,lastName,activity, pageno, sortField,sortDir);
        List<Audit> emailLogList =  page.getContent();
      
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Audit> dtoList = new PaginationDto<Audit>(emailLogList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Audit> getAuditPage(Date startDate, Date endDate, String empId, String firstName, String lastName,
			String activity, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Audit> dateSpec = generalSpecificationUtil.dateSpecification(startDate, endDate, ApplicationConstants.DATE);
		Specification<Audit> empIdSpc = generalSpecificationUtil.stringSpecification(empId, "employeeId");
		Specification<Audit> fNameSpc = generalSpecificationUtil.stringSpecification(firstName, "firstName");
		Specification<Audit> lNameSpc = generalSpecificationUtil.stringSpecification(lastName, "lastName");
		Specification<Audit> activitySpc = generalSpecificationUtil.stringSpecification(activity, "activity");
		Page<Audit> page = auditRepository.findAll(fNameSpc.and(dateSpec).and(empIdSpc).and(lNameSpc).and(activitySpc),pageable);
		return page;
	}

}

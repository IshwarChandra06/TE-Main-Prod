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
import com.eikona.tech.entity.EmailLogs;
import com.eikona.tech.repository.EmailLogsRepository;
import com.eikona.tech.service.EmailLogService;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class EmailLogServiceImpl implements EmailLogService{

	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private EmailLogsRepository emailLogsRepository;
	
	@Autowired
	private GeneralSpecificationUtil<EmailLogs> generalSpecificationEmailLog;
	
	@Override
	public PaginationDto<EmailLogs> searchByField(String sDate, String eDate, String mailId, String type, int pageno,String sortField, String sortDir) {
		
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
		
		Page<EmailLogs> page = getEmailLogPage(startDate,endDate, mailId, type, pageno, sortField,sortDir);
        List<EmailLogs> emailLogList =  page.getContent();
      
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<EmailLogs> dtoList = new PaginationDto<EmailLogs>(emailLogList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<EmailLogs> getEmailLogPage(Date startDate, Date endDate, String mailId, String type, int pageno,
			String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<EmailLogs> dateSpec = generalSpecificationEmailLog.dateSpecification(startDate, endDate, ApplicationConstants.DATE);
		Specification<EmailLogs> mailIdSpc = generalSpecificationEmailLog.stringSpecification(mailId, "managerEmailId");
		Specification<EmailLogs> typeSpec = generalSpecificationEmailLog.stringSpecification(type, "type");
		
    	Page<EmailLogs> page = emailLogsRepository.findAll(mailIdSpc.and(dateSpec).and(typeSpec),pageable);
		return page;
	}

}

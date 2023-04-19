package com.eikona.tech.service.impl;

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
import com.eikona.tech.constants.EmailSetupConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Blacklist;
import com.eikona.tech.entity.EmailLogs;
import com.eikona.tech.entity.EmailSetup;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.repository.BlacklistRepository;
import com.eikona.tech.repository.EmailLogsRepository;
import com.eikona.tech.repository.EmailSetupRepository;
import com.eikona.tech.service.BlacklistService;
import com.eikona.tech.service.EmployeeService;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class BlacklistServiceImpl implements BlacklistService {
	
	@Autowired
	private BlacklistRepository blacklistRepository;
	
	@Autowired
	private EmailLogsRepository emailLogsRepository;
	
	@Autowired
	private EmployeeService employeeService;
	
	@Autowired
	private GeneralSpecificationUtil<Blacklist> generalSpecificationBlacklist;
	
	@Autowired
	private EmailSetupServiceImpl emailSetupServiceImpl;
	
	@Autowired
	private EmailSetupRepository emailSetupRepository;
	

	@Override
	public PaginationDto<Blacklist> searchByField(String empId, String orderBy, int pageno,
			String sortField, String sortDir) {
		

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Blacklist> empIdSpc = generalSpecificationBlacklist.foreignKeyStringSpecification(empId, "employee", EmployeeConstants.EMPLOYEE_ID);
		Specification<Blacklist> orderBySpec = generalSpecificationBlacklist.stringSpecification(orderBy, "orderBy");
		Specification<Blacklist> statusSpec = generalSpecificationBlacklist.stringSpecification("Blacklisted", "status");
		
    	Page<Blacklist> page = blacklistRepository.findAll(empIdSpc.and(orderBySpec).and(statusSpec),pageable);
    	 List<Blacklist> blackList =  page.getContent();
         
 		
 		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
 		PaginationDto<Blacklist> dtoList = new PaginationDto<Blacklist>(blackList, page.getTotalPages(),
 				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
 		return dtoList;
	}
	
	@Override
	public Blacklist save(Blacklist blacklist) {
		
		Employee emp=blacklist.getEmployee();
		if("Blacklisted".equalsIgnoreCase(blacklist.getStatus())) {
			List<AccessLevel> accLevel=new ArrayList<AccessLevel>();
			emp.setAccessLevel(accLevel);
			EmailSetup emailSetup =  emailSetupRepository.findById(6l).get();
			
			String body = EmailSetupConstants.BLACK_LISTING.formatted(emailSetup.getName(), emp.getEmployeeId(),
					emp.getFirstName()+" "+emp.getLastName(),blacklist.getStartDateStr(), blacklist.getReason());
			try {
				emailSetupServiceImpl.sendEmail(emailSetup, body);
				
				EmailLogs emailLogs = new EmailLogs();
				emailLogs.setDate(new Date());
				emailLogs.setType(emailSetup.getSubject());
				emailLogs.setToEmailId(emailSetup.getTo());
				emailLogsRepository.save(emailLogs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		if(null!=blacklist.getId()) {
			Blacklist bl=blacklistRepository.findById(blacklist.getId()).get();
			blacklist.setCreatedBy(bl.getCreatedBy());
			blacklist.setCreatedDate(bl.getCreatedDate());
			
			if((!blacklist.getRemovalDate().isEmpty()) &&"Suspended".equalsIgnoreCase(blacklist.getStatus())) {
				blacklist.setRemove(true);
				blacklistRepository.save(blacklist);
				emp.setStatus("Active");
			}
		}else if("Suspended".equalsIgnoreCase(blacklist.getStatus())) {
			EmailSetup emailSetup =  emailSetupRepository.findById(7l).get();
			
			String body = EmailSetupConstants.SUSPENSION.formatted(emailSetup.getName(), emp.getEmployeeId(),
					emp.getFirstName()+" "+emp.getLastName(),blacklist.getStartDateStr(),blacklist.getEndDateStr(), blacklist.getReason());
			try {
				emailSetupServiceImpl.sendEmail(emailSetup, body);
				EmailLogs emailLogs = new EmailLogs();
				emailLogs.setDate(new Date());
				emailLogs.setType(emailSetup.getSubject());
				emailLogs.setToEmailId(emailSetup.getTo());
				emailLogsRepository.save(emailLogs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		employeeService.save(emp);
		return blacklistRepository.save(blacklist);
	}

	@Override
	public PaginationDto<Blacklist> searchByField(String employeeId, int pageno, String sortField, String sortDir) {

		

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Blacklist> empIdSpc = generalSpecificationBlacklist.foreignKeyStringSpecification(employeeId, "employee", EmployeeConstants.EMPLOYEE_ID);
		Specification<Blacklist> blacklistSpec = generalSpecificationBlacklist.stringSpecification("Blacklisted", "status");
		Specification<Blacklist> suspendSpec = generalSpecificationBlacklist.stringSpecification("Suspended","status");
		
    	Page<Blacklist> page = blacklistRepository.findAll(empIdSpc.and(blacklistSpec.or(suspendSpec)),pageable);
    	 List<Blacklist> blackList =  page.getContent();
         
 		
 		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
 		PaginationDto<Blacklist> dtoList = new PaginationDto<Blacklist>(blackList, page.getTotalPages(),
 				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
 		return dtoList;
	
	}

	@Override
	public PaginationDto<Blacklist> searchByField(String employeeId, String status, String orderBy, int pageno,
			String sortField, String sortDir) {
		

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Blacklist> empIdSpc = generalSpecificationBlacklist.foreignKeyStringSpecification(employeeId, "employee", EmployeeConstants.EMPLOYEE_ID);
		Specification<Blacklist> orderBySpec = generalSpecificationBlacklist.stringSpecification(orderBy, "orderBy");
		Specification<Blacklist> statusSpec = generalSpecificationBlacklist.stringSpecification(status, "status");
		
    	Page<Blacklist> page = blacklistRepository.findAll(empIdSpc.and(orderBySpec).and(statusSpec),pageable);
    	 List<Blacklist> blackList =  page.getContent();
         
 		
 		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
 		PaginationDto<Blacklist> dtoList = new PaginationDto<Blacklist>(blackList, page.getTotalPages(),
 				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
 		return dtoList;
	}

}

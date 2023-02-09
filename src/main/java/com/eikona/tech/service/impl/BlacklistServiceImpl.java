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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Blacklist;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.repository.BlacklistRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.TransactionRepository;
import com.eikona.tech.service.BlacklistService;
import com.eikona.tech.service.EmployeeService;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
@EnableScheduling
public class BlacklistServiceImpl implements BlacklistService {
	
	@Autowired
	private BlacklistRepository blacklistRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private EmployeeService employeeService;
	
	@Autowired
	private GeneralSpecificationUtil<Blacklist> generalSpecificationBlacklist;
	

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
	
//	@Scheduled(cron="0 0 5 * * *")
//	@Scheduled(fixedDelay = 5000)
	public void setEmployeeStatusInactive() throws ParseException {
		List<Employee> employeeList=employeeRepository.findAllByStatus("Active");
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		String today=dateFormat.format(new Date());
		Date fromDate=calendarUtil.getNextOrPreviousDate(dateFormat.parse(today), -30, 0, 0, 0);
		Date toDate=calendarUtil.getConvertedDate(dateFormat.parse(today), 23, 00, 00);
		for(Employee employee:employeeList) {
			Long count=transactionRepository.findByDateAndEmpIdCustom(fromDate,toDate,employee.getEmployeeId());
			if(count==0){
				List<AccessLevel> accLevelList= new ArrayList<>();
				employee.setStatus("Inactive");
				employee.setAccessLevel(accLevelList);
				employeeRepository.save(employee);
				}
			}
	}

	@Override
	public Blacklist save(Blacklist blacklist) {
		
		Employee emp=blacklist.getEmployee();
		if("Blacklisted".equalsIgnoreCase(blacklist.getStatus())) {
			List<AccessLevel> accLevel=new ArrayList<AccessLevel>();
			emp.setAccessLevel(accLevel);
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

package com.eikona.tech.service.impl;

import java.sql.ResultSet;
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
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.constants.TransactionConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Transaction;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.TransactionRepository;
import com.eikona.tech.service.TransactionService;
import com.eikona.tech.util.BioSecurityServerUtil;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
@EnableScheduling
public class TransactionServiceImpl implements TransactionService{
	
	
	@Autowired
	private GeneralSpecificationUtil<Transaction> generalSpecification;

	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private BioSecurityServerUtil bioSecurityServerUtil; 



	@Override
	public PaginationDto<Transaction> searchByField(String sDate, String eDate, String employeeId,
			String employeeName, String employeeType,String department, String designation, String device, int pageno,String sortField, String sortDir) {
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

		Page<Transaction> page = getTransactionBySpecification(employeeId, employeeName, employeeType,department,designation,device, pageno, sortField, sortDir, startDate, endDate);
		List<Transaction> employeeShiftList = page.getContent();

		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC : ApplicationConstants.ASC;
		PaginationDto<Transaction> dtoList = new PaginationDto<Transaction>(employeeShiftList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir,
				ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Transaction> getTransactionBySpecification(String employeeId, String employeeName,
			String employeeType,  String department, String designation, String device, int pageno, String sortField,String sortDir, Date startDate, Date endDate) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno -NumberConstants.ONE , NumberConstants.TEN, sort);

		Specification<Transaction> dateSpec = generalSpecification.dateSpecification(startDate, endDate,
				TransactionConstants.PUNCH_DATE);
		Specification<Transaction> empIdSpec = generalSpecification.stringSpecification(employeeId, TransactionConstants.EMP_ID);
		Specification<Transaction> empNameSpec = generalSpecification.stringSpecification(employeeName, ApplicationConstants.NAME);
		Specification<Transaction> employeeTypeSpec = generalSpecification.stringSpecification(employeeType,"employeeType");
		Specification<Transaction> deptSpec = generalSpecification.stringSpecification(employeeId, TransactionConstants.DEPARTMENT);
		Specification<Transaction> desigSpec = generalSpecification.stringSpecification(employeeName, TransactionConstants.DESIGNATION);
		Specification<Transaction> deviceSpec = generalSpecification.stringSpecification(employeeType,TransactionConstants.DEVICE);
		

		Page<Transaction> page = transactionRepository.findAll(dateSpec.and(empIdSpec).and(empNameSpec).and(deviceSpec).and(desigSpec).and(deptSpec)
				.and(employeeTypeSpec), pageable);
		return page;
	}

//	@Scheduled(cron="0 0 5 * * *")
//	@Scheduled(fixedDelay = 5000)
	public void setEmployeeStatusInactive(){
		try {
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
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
//	@Scheduled(cron="0 0 0/1 * * ?")
//	@Scheduled(fixedDelay = 50000)
	public void syncTransactionFromZKDatabase() {
		Date currTime=new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US);
		SimpleDateFormat datetimeFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US_WITH_MILLISECOND);
		SimpleDateFormat timeFormat = new SimpleDateFormat(ApplicationConstants.TIME_FORMAT_24HR);
		String currTimeStr=sdf.format(currTime);
		
		Date oneHrBefore=calendarUtil.getDateByAddingHour(currTime, -1);
		String startTimeStr=sdf.format(oneHrBefore);
		
		try {
			
			String query = "select * from acc_transaction where event_time between '"+startTimeStr+"' and '"+currTimeStr+"'";
			ResultSet resultSet = bioSecurityServerUtil.jdbcConnection(query);
			List<Transaction> transList= new ArrayList<>();
			if(null != resultSet) {
				while (resultSet.next()) {
					Transaction trans= new Transaction();
					String firstName="";
					String lastName="";
					if(null!=resultSet.getString("name"))
						firstName=resultSet.getString("name");
					if(null!=resultSet.getString("last_name"))
						lastName=resultSet.getString("last_name");
					trans.setName(firstName+" "+lastName);
					
					trans.setEmpId(resultSet.getString("pin"));
					String punchDateStr=resultSet.getString("event_time");
					trans.setPunchDate(datetimeFormat.parse(punchDateStr));
					trans.setPunchTime(timeFormat.parse(punchDateStr));
					trans.setArea(resultSet.getString("area_name"));
					trans.setDeviceId(resultSet.getString("dev_sn"));
					trans.setDeviceName(resultSet.getString("dev_alias"));
					trans.setDepartment(resultSet.getString("dept_name"));
					
					if(null!=trans.getEmpId() && !trans.getEmpId().isEmpty()) {
						Employee employee=employeeRepository.findByEmployeeId(trans.getEmpId());
						trans.setDepartment(employee.getDepartment());
						trans.setDesignation(employee.getDesignation());
						if(null!=employee.getEmployeeType())
						 trans.setEmployeeType(employee.getEmployeeType().getName());
					}
					transList.add(trans); 
				}
				transactionRepository.saveAll(transList);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

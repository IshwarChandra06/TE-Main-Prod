package com.eikona.tech.service.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Audit;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.LastSyncStatus;
import com.eikona.tech.repository.AccessLevelRepository;
import com.eikona.tech.repository.AuditRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.LastSyncStatusRepository;
import com.eikona.tech.util.BioSecurityServerUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
@EnableScheduling
public class ActiveEmployeeServiceImpl {
	
	
	@Autowired
	private GeneralSpecificationUtil<Employee> generalSpecificationEmployee;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private InactiveEmployeeServiceImpl inactiveEmployeeServiceImpl;
	
	@Autowired
	private BioSecurityServerUtil bioSecurityServerUtil;
	
	@Autowired
	private AccessLevelRepository accessLevelRepository;
	
	@Autowired
	private LastSyncStatusRepository lastSyncStatusRepository;
	
	@Autowired
	private AuditRepository auditRepository;
	
	public void updateAllActiveEmployeeFromSF(){
		
		try {
			Date currentDate= new Date();
			List<Employee> employeeList=employeeRepository.findAllByStatus("Active");
			AccessLevel acclevel=accessLevelRepository.findByName("New Employee");
			List<AccessLevel> accLevelList=new ArrayList<AccessLevel>();
			accLevelList.add(acclevel);
			for(Employee employee:employeeList) {
					JSONObject dataObject= bioSecurityServerUtil.getEmployeeFromBioSecurity(employee.getEmployeeId());
					if(null!=dataObject) 
						setEmployeeDetailsFromBSToMata(employee, dataObject);
			        else 
			        	employee = addActiveEmployeeInBiosecurity(accLevelList, employee);
			        		
			  }
			LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate,"Push Active Employee To BS");
			lastSyncStatusRepository.save(lastSyncStatus);
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Scheduled(cron = "0 0 0/7 * * ?")
	public void updateLastUpdatedActiveEmployeeFromSF(){
		try {
			Date currentDate= new Date();
			LastSyncStatus lastSync = lastSyncStatusRepository.findByActivity("Push Active Employee To BS");
			List<Employee> employeeList=employeeRepository.findAllByStatusAndLastUpdatedTimeCustom(lastSync.getLastSyncTime(),currentDate,"Active");
			AccessLevel acclevel=accessLevelRepository.findByName("New Employee");
			List<AccessLevel> accLevelList=new ArrayList<AccessLevel>();
			accLevelList.add(acclevel);
			for(Employee employee:employeeList) {
					JSONObject dataObject= bioSecurityServerUtil.getEmployeeFromBioSecurity(employee.getEmployeeId());
					if(null!=dataObject) 
						setEmployeeDetailsFromBSToMata(employee, dataObject);
			        else 
			        	employee = addActiveEmployeeInBiosecurity(accLevelList, employee);
			        		
			  }
			LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate,"Push Active Employee To BS");
			lastSyncStatusRepository.save(lastSyncStatus);
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void setEmployeeDetailsFromBSToMata(Employee employee, JSONObject dataObject) {
		System.out.println(employee.getEmployeeId());
		String cardNo=(String) dataObject.get("cardNo");
		String accessLevelIds=(String) dataObject.get("accLevelIds");
		if(null!=accessLevelIds && !accessLevelIds.isEmpty()) {
			String[] splitByComma=accessLevelIds.split(",");
			List<AccessLevel> accesslevelList= new ArrayList<>();
			for(String accessId:splitByComma) {
				AccessLevel accesslevel = accessLevelRepository.findByAccessId(accessId);
				accesslevelList.add(accesslevel);
			}
			employee.setAccessLevel(accesslevelList);
		}
		employee.setCardId(cardNo);
		employeeRepository.save(employee);
	}
	private Employee addActiveEmployeeInBiosecurity(List<AccessLevel> accLevelList, Employee employee) throws Exception {
		employee.setAccessLevel(accLevelList);
		employee=employeeRepository.save(employee);
		String msg=bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
		if("success".equalsIgnoreCase(msg)) {
			Audit audit= new Audit();
			audit.setDate(new Date());
			audit.setEmployeeId(employee.getEmployeeId());
			audit.setFirstName(employee.getFirstName());
			audit.setLastName(employee.getLastName());
			audit.setType("Add");
			audit.setActivity("Push Active Employee To BS");
			auditRepository.save(audit);
		}
		return employee;
	}
	private LastSyncStatus setLastSyncStatus(Date currentDate,String activity) {
		LastSyncStatus lastSyncStatus = lastSyncStatusRepository.findByActivity(activity);
		if(null!=lastSyncStatus)
			lastSyncStatus.setLastSyncTime(currentDate);
		else {
			    lastSyncStatus = new LastSyncStatus();
				lastSyncStatus.setActivity(activity);
				lastSyncStatus.setLastSyncTime(currentDate);
		}
		return lastSyncStatus;
	}

	public PaginationDto<Employee> searchByField(String firstName, String lastName,String empId,String department,String designation,
			String employeeType,String cardNo, int pageno, String sortField, String sortDir) {

		

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Employee> page = getEmployeePage(firstName, lastName, empId, department, designation,employeeType,cardNo, pageno, sortField, sortDir);
        List<Employee> employeeList =  page.getContent();
      
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Employee> dtoList = new PaginationDto<Employee>(employeeList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	
	}

	private Page<Employee> getEmployeePage(String firstName, String lastName,String empId,String department,String designation,
			String employeeType,String cardNo, int pageno, String sortField, String sortDir) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Employee> isDeletedFalse = generalSpecificationEmployee.isDeletedSpecification(false);
		Specification<Employee> firstNameSpc = generalSpecificationEmployee.stringSpecification(firstName, EmployeeConstants.FIRST_NAME);
		Specification<Employee> lastNameSpc = generalSpecificationEmployee.stringSpecification(lastName, EmployeeConstants.LAST_NAME);
		Specification<Employee> empIdSpc = generalSpecificationEmployee.stringSpecification(empId, EmployeeConstants.EMPLOYEE_ID);
		Specification<Employee> deptSpec = generalSpecificationEmployee.stringSpecification(department, EmployeeConstants.DEPARTMENT);
		Specification<Employee> designationSpc = generalSpecificationEmployee.stringSpecification(designation, EmployeeConstants.DESIGNATION);
		Specification<Employee> employeeTypSpc = generalSpecificationEmployee.foreignKeyStringSpecification(employeeType, EmployeeConstants.EMPLOYEE_TYPE,ApplicationConstants.NAME);
		Specification<Employee> CardSpc = generalSpecificationEmployee.stringSpecification(cardNo, EmployeeConstants.CARD_ID);
		Specification<Employee> activeSpc = generalSpecificationEmployee.stringEqualSpecification("Active", EmployeeConstants.STATUS);
		Specification<Employee> transferSpc = generalSpecificationEmployee.stringEqualSpecification("Transfer", EmployeeConstants.STATUS);
		
    	Page<Employee> page = employeeRepository.findAll(CardSpc.and(CardSpc).and(empIdSpc).and(deptSpec).and(designationSpc).and(activeSpc.or(transferSpc)).and(isDeletedFalse).and(employeeTypSpc).and(firstNameSpc).and(lastNameSpc), pageable);
		return page;
	
	}

	public void fileExportBySearchValue(HttpServletResponse response, String firstName, String lastName,String empId,String department,String designation,
			String employeeType,String cardNo, String flag) {
		
		
		List<Employee> employeeList = getListOfEmployee(firstName, lastName, empId, department,  designation, employeeType,cardNo);
		
		try {
			inactiveEmployeeServiceImpl.excelGenerator(response, employeeList);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	public List<Employee> getListOfEmployee(String firstName, String lastName,String empId,String department,String designation,
			String employeeType,String cardNo) {
		
		Specification<Employee> isDeletedFalse = generalSpecificationEmployee.isDeletedSpecification(false);
		Specification<Employee> firstNameSpc = generalSpecificationEmployee.stringSpecification(firstName, EmployeeConstants.FIRST_NAME);
		Specification<Employee> lastNameSpc = generalSpecificationEmployee.stringSpecification(lastName, EmployeeConstants.LAST_NAME);
		Specification<Employee> empIdSpc = generalSpecificationEmployee.stringSpecification(empId, EmployeeConstants.EMPLOYEE_ID);
		Specification<Employee> deptSpec = generalSpecificationEmployee.stringSpecification(department, EmployeeConstants.DEPARTMENT);
		Specification<Employee> designationSpc = generalSpecificationEmployee.stringSpecification(designation, EmployeeConstants.DESIGNATION);
		Specification<Employee> employeeTypSpc = generalSpecificationEmployee.foreignKeyStringSpecification(employeeType, EmployeeConstants.EMPLOYEE_TYPE,ApplicationConstants.NAME);
		Specification<Employee> CardSpc = generalSpecificationEmployee.stringSpecification(cardNo, EmployeeConstants.CARD_ID);
		Specification<Employee> activeSpc = generalSpecificationEmployee.stringEqualSpecification("Active", EmployeeConstants.STATUS);
		Specification<Employee> transferSpc = generalSpecificationEmployee.stringEqualSpecification("Transfer", EmployeeConstants.STATUS);
		
		List<Employee> employeeList = employeeRepository.findAll(CardSpc.and(CardSpc).and(empIdSpc).and(deptSpec).and(designationSpc).and(isDeletedFalse).and(employeeTypSpc).and(activeSpc.or(transferSpc)).and(firstNameSpc).and(lastNameSpc));
		return employeeList;
	}
}

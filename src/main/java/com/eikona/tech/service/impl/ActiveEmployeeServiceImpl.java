package com.eikona.tech.service.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class ActiveEmployeeServiceImpl {
	
	
	@Autowired
	private GeneralSpecificationUtil<Employee> generalSpecificationEmployee;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private InactiveEmployeeServiceImpl inactiveEmployeeServiceImpl;

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
		Specification<Employee> statusSpc = generalSpecificationEmployee.stringEqualSpecification("Active", EmployeeConstants.STATUS);
		
    	Page<Employee> page = employeeRepository.findAll(CardSpc.and(CardSpc).and(empIdSpc).and(deptSpec).and(designationSpc).and(statusSpc).and(isDeletedFalse).and(employeeTypSpc).and(firstNameSpc).and(lastNameSpc), pageable);
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
		Specification<Employee> statusSpc = generalSpecificationEmployee.stringEqualSpecification("Active", EmployeeConstants.STATUS);
		
		List<Employee> employeeList = employeeRepository.findAll(CardSpc.and(CardSpc).and(empIdSpc).and(deptSpec).and(designationSpc).and(isDeletedFalse).and(employeeTypSpc).and(statusSpc).and(firstNameSpc).and(lastNameSpc));
		return employeeList;
	}
}

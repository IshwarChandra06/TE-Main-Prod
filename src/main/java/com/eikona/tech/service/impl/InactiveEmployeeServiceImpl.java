package com.eikona.tech.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import com.eikona.tech.constants.HeaderConstants;
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
public class InactiveEmployeeServiceImpl {
	
	@Autowired
	private GeneralSpecificationUtil<Employee> generalSpecificationEmployee;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private BioSecurityServerUtil bioSecurityServerUtil;
	
	@Autowired
	private AccessLevelRepository accessLevelRepository;
	
	@Autowired
	private LastSyncStatusRepository lastSyncStatusRepository;
	
	@Autowired
	private AuditRepository auditRepository;

	public PaginationDto<Employee> searchByField(String firstName, String lastName,String empId,String department,String designation,
			String employeeType,String cardNo, int pageno, String sortField, String sortDir) {

		
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Employee> page = getEmployeePage(firstName, lastName, empId, department,  designation, employeeType,cardNo, pageno, sortField,
				sortDir);
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
		Specification<Employee> statusSpc = generalSpecificationEmployee.stringEqualSpecification("Inactive", EmployeeConstants.STATUS);
		
    	Page<Employee> page = employeeRepository.findAll(CardSpc.and(CardSpc).and(empIdSpc).and(deptSpec).and(designationSpc).and(statusSpc).and(isDeletedFalse).and(employeeTypSpc).and(firstNameSpc).and(lastNameSpc),pageable);
		return page;
	}

	public void fileExportBySearchValue(HttpServletResponse response, String firstName, String lastName,String empId,String department,String designation,
			String employeeType,String cardNo, String flag) {
		
		List<Employee> employeeList = getListOfEmployee(firstName, lastName, empId, department,  designation, employeeType,cardNo);
		
		try {
			excelGenerator(response, employeeList);
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
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
    	Specification<Employee> statusSpc = generalSpecificationEmployee.stringEqualSpecification("Inactive", EmployeeConstants.STATUS);
		List<Employee> employeeList =employeeRepository.findAll(CardSpc.and(CardSpc).and(empIdSpc).and(deptSpec).and(designationSpc).and(statusSpc).and(isDeletedFalse).and(employeeTypSpc).and(firstNameSpc).and(lastNameSpc));
		return employeeList;
	}
	
	public void excelGenerator(HttpServletResponse response, List<Employee> employeeList)
			throws ParseException, IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Employee_Status_Report_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);

		CellStyle cellStyle = setBorderStyle(workBook, BorderStyle.THICK, font);

		setHeaderForExcel(row, cellStyle);

		font = workBook.createFont();
		font.setBold(false);
		cellStyle = setBorderStyle(workBook, BorderStyle.THIN, font);
		
		//set data for excel
		setExcelDataCellWise(employeeList, sheet, rowCount, cellStyle);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}
	
	public CellStyle setBorderStyle(Workbook workBook, BorderStyle borderStyle, Font font) {
		CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setBorderTop(borderStyle);
		cellStyle.setBorderBottom(borderStyle);
		cellStyle.setBorderLeft(borderStyle);
		cellStyle.setBorderRight(borderStyle);
		cellStyle.setFont(font);
		return cellStyle;
	}
	private void setExcelDataCellWise(List<Employee> employeeList, Sheet sheet, int rowCount,
			CellStyle cellStyle) {
		
		for (Employee employee : employeeList) {
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;

			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getEmployeeId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getFirstName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getLastName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getCardId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getDepartment());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getDesignation());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=employee.getEmployeeType())
			 cell.setCellValue(employee.getEmployeeType().getName());
			else
			 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getContactNo());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getEmailId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getCadre());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getStatus());
			cell.setCellStyle(cellStyle);
		}
	}
	
	
	private void setHeaderForExcel(Row row, CellStyle cellStyle) {
		Cell cell = row.createCell(NumberConstants.ZERO);
		cell.setCellValue(HeaderConstants.EMPLOYEE_ID);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.ONE);
		cell.setCellValue(HeaderConstants.FIRST_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWO);
		cell.setCellValue(HeaderConstants.LAST_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.THREE);
		cell.setCellValue(HeaderConstants.CARD_NO);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FOUR);
		cell.setCellValue(HeaderConstants.DEPARTMENT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIVE);
		cell.setCellValue(HeaderConstants.DESIGNATION);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SIX);
		cell.setCellValue(HeaderConstants.EMPLOYEE_TYPE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SEVEN);
		cell.setCellValue(HeaderConstants.CONTACT_NO);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.EIGHT);
		cell.setCellValue(HeaderConstants.EMAIL);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.NINE);
		cell.setCellValue(HeaderConstants.CADRE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TEN);
		cell.setCellValue(HeaderConstants.STATUS);
		cell.setCellStyle(cellStyle);
	}
	
	public void removeAccessLevelOfInactiveEmployeeFromSF(){
		try {
			Date currentDate= new Date();
			List<Employee> employeeList=employeeRepository.findAllByStatus("Inactive");
			AccessLevel acclevel=accessLevelRepository.findByName("No Door");
			List<AccessLevel> accLevelList=new ArrayList<AccessLevel>();
			accLevelList.add(acclevel);
			List<Audit> auditList=new ArrayList<Audit>();
			for(Employee employee:employeeList) {
				
					setNoAccessLevelToInactiveEmployee(accLevelList, auditList, employee);
					
					auditRepository.saveAll(auditList);
					
					LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate,"Push Inactive Employee To BS");
					lastSyncStatusRepository.save(lastSyncStatus);
			  }
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Scheduled(cron = "0 0 0/8 * * ?")
	public void lastUpdatedInactiveEmployeeFromSF(){
		try {
			Date currentDate= new Date();
			LastSyncStatus lastSync = lastSyncStatusRepository.findByActivity("Push Inactive Employee To BS");
			List<Employee> employeeList=employeeRepository.findAllByStatusAndLastUpdatedTimeCustom(lastSync.getLastSyncTime(),currentDate,"Inactive");
			AccessLevel acclevel=accessLevelRepository.findByName("No Door");
			List<AccessLevel> accLevelList=new ArrayList<AccessLevel>();
			accLevelList.add(acclevel);
			List<Audit> auditList=new ArrayList<Audit>();
			for(Employee employee:employeeList) {
				
				setNoAccessLevelToInactiveEmployee(accLevelList, auditList, employee);
				
				auditRepository.saveAll(auditList);
				
					LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate,"Push Inactive Employee To BS");
					lastSyncStatusRepository.save(lastSyncStatus);
			  }
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setNoAccessLevelToInactiveEmployee(List<AccessLevel> accLevelList, List<Audit> auditList,
			Employee employee) throws Exception {
		JSONObject dataObject= bioSecurityServerUtil.getEmployeeFromBioSecurity(employee.getEmployeeId());
		employee.setAccessLevel(accLevelList);
		if(null!=dataObject) {
			System.out.println(employee.getEmployeeId());
			String cardNo=(String) dataObject.get("cardNo");
			employee.setCardId(cardNo);
			employeeRepository.save(employee);
			String msg=bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
			if("success".equalsIgnoreCase(msg)) {
				Audit audit= new Audit();
				audit.setDate(new Date());
				audit.setEmployeeId(employee.getEmployeeId());
				audit.setFirstName(employee.getFirstName());
				audit.setLastName(employee.getLastName());
				audit.setType("Update");
				audit.setActivity("Remove Access From Inactive Employee");
				auditList.add(audit);
			}
		}
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
}

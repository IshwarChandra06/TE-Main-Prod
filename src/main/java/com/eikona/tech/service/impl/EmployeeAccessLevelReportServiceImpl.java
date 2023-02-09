package com.eikona.tech.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Component
public class EmployeeAccessLevelReportServiceImpl {
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Employee> generalSpecificationEmployee;

	public PaginationDto<Employee> searchByField(String plant, String building, String[] accessLevels, String zone, int pageno,
			String sortField, String sortDir) {

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Employee> page = getEmployeePage(plant,building, accessLevels,zone, pageno, sortField, sortDir);
        List<Employee> employeeList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Employee> dtoList = new PaginationDto<Employee>(employeeList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	
	}

	private Page<Employee> getEmployeePage(String plant, String building, String[] accessLevels, String zone, int pageno,
			String sortField, String sortDir) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Employee> isDeletedFalse = generalSpecificationEmployee.isDeletedSpecification(false);
		Specification<Employee> accessSpec = generalSpecificationEmployee.foreignKeySpecification(accessLevels, EmployeeConstants.ACCESS_LEVEL, "name");
		Specification<Employee> buildingSpc = generalSpecificationEmployee.foreignKeySpecification(building, EmployeeConstants.ACCESS_LEVEL, EmployeeConstants.BUILDING, "name");
		Specification<Employee> zoneSpc = generalSpecificationEmployee.foreignKeySpecification(zone, EmployeeConstants.ACCESS_LEVEL, "zone", "name");
		Specification<Employee> plantSpc = generalSpecificationEmployee.foreignKeySpecification(plant, EmployeeConstants.ACCESS_LEVEL, EmployeeConstants.BUILDING, EmployeeConstants.PLANT, "name");
		
		Page<Employee> page = employeeRepository.findAll(plantSpc.and(buildingSpc).and(accessSpec).and(isDeletedFalse).and(zoneSpc), pageable);
		return page;
	
	}

	public void fileExportBySearchValue(HttpServletResponse response, String plant, String building,
			String[] accessLevels, String zone, String flag) throws ParseException, IOException {
		
		Specification<Employee> isDeletedFalse = generalSpecificationEmployee.isDeletedSpecification(false);
		Specification<Employee> accessSpec = generalSpecificationEmployee.foreignKeySpecification(accessLevels, EmployeeConstants.ACCESS_LEVEL, "name");
		Specification<Employee> buildingSpc = generalSpecificationEmployee.foreignKeySpecification(building, EmployeeConstants.ACCESS_LEVEL, EmployeeConstants.BUILDING, "name");
		Specification<Employee> plantSpc = generalSpecificationEmployee.foreignKeySpecification(plant, EmployeeConstants.ACCESS_LEVEL, EmployeeConstants.BUILDING, EmployeeConstants.PLANT, "name");
		Specification<Employee> zoneSpc = generalSpecificationEmployee.foreignKeySpecification(zone, EmployeeConstants.ACCESS_LEVEL, "zone", "name");
		List<Employee> employeeList =employeeRepository.findAll(plantSpc.and(buildingSpc).and(accessSpec).and(isDeletedFalse).and(zoneSpc));
		
		excelGenerator(response, employeeList);
		
	}
	
	
	
	public void excelGenerator(HttpServletResponse response, List<Employee> employeeList)
			throws ParseException, IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = EmployeeConstants.EMPLOYEE_MASTER_DATA + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
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
			cell.setCellValue(employee.getId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
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
			cell.setCellValue(employee.getPalntStr());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getBuildingStr());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getAccessLevelStr());
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
			cell.setCellValue(employee.getPayGrade());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(setDateFormat(employee.getJoinDate()));
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(setDateFormat(employee.getEndDate()));
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(setDateTimeFormat(employee.getLastModifiedDate()));
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getStatus());
			cell.setCellStyle(cellStyle);
		}
	}
	
	private String setDateFormat(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		String dareStr="";
		if(null!=date) {
			dareStr=dateFormat.format(date);
		}
		return dareStr;
		
	}
    private String setDateTimeFormat(Date date) {
    	SimpleDateFormat dateTimeFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US);
    	String dareStr="";
		if(null!=date) {
			dareStr=dateTimeFormat.format(date);
		}
		return dareStr;
	}
	private void setHeaderForExcel(Row row, CellStyle cellStyle) {
		Cell cell = row.createCell(NumberConstants.ZERO);
		cell.setCellValue(HeaderConstants.ID);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.ONE);
		cell.setCellValue(HeaderConstants.EMPLOYEE_ID);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWO);
		cell.setCellValue(HeaderConstants.FIRST_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.THREE);
		cell.setCellValue(HeaderConstants.LAST_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FOUR);
		cell.setCellValue(HeaderConstants.CARD_NO);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIVE);
		cell.setCellValue(HeaderConstants.DEPARTMENT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SIX);
		cell.setCellValue(HeaderConstants.DESIGNATION);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SEVEN);
		cell.setCellValue(HeaderConstants.PLANT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.EIGHT);
		cell.setCellValue(HeaderConstants.BUILDING);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.NINE);
		cell.setCellValue(HeaderConstants.ACCESS_LEVELS);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TEN);
		cell.setCellValue(HeaderConstants.EMPLOYEE_TYPE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.ELEVEN);
		cell.setCellValue(HeaderConstants.CONTACT_NO);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWELVE);
		cell.setCellValue(HeaderConstants.EMAIL);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.THIRTEEN);
		cell.setCellValue(HeaderConstants.CADRE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FOURTEEN);
		cell.setCellValue(HeaderConstants.PAY_GRADE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIFTEEN);
		cell.setCellValue(HeaderConstants.JOIN_DATE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SIXTEEN);
		cell.setCellValue(HeaderConstants.END_DATE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SEVENTEEN);
		cell.setCellValue(HeaderConstants.LAST_MODIFIED_DATE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.EIGHTEEN);
		cell.setCellValue(HeaderConstants.STATUS);
		cell.setCellStyle(cellStyle);
	}
}

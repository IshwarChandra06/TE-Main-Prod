package com.eikona.tech.export;

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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;


@Component
public class ExportMetalException {
	
	@Autowired
	private ExportEmployeeMasterData exportEmployee;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private ExportMetalExceptionPdfUtil exportMetalExceptionPdf;
	
	@Autowired
	private ExportMetalExceptionCsvUtil exportMetalExceptionCsv;
	
	@Autowired
	private GeneralSpecificationUtil<Employee> generalSpecification;

	public void metalExceptionExportBySearchValue(HttpServletResponse response,String sDate,String eDate,  String firstName, String lastName,
			String employeeId,String department,String designation,String employeeType,String cardNo, String lanyard,String status,String flag) throws ParseException, IOException {
		
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
		
		List<Employee> employeeList = getListOfEmployee(firstName,lastName, employeeId, department,designation,employeeType,cardNo,lanyard,status,  startDate, endDate);
		
		if("xlsx".equalsIgnoreCase(flag))
			excelGenerator(response, employeeList);
		else if("pdf".equalsIgnoreCase(flag))
			exportMetalExceptionPdf.pdfGenerator(response, employeeList);
		else
			exportMetalExceptionCsv.csvGenerator(response, employeeList);
		
	}
	public List<Employee> getListOfEmployee( String firstName, String lastName, String employeeId, String department, String designation,
			 String employeeType,String cardNo, String lanyard, String status,Date startDate, Date endDate) {
		Specification<Employee> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		Specification<Employee> containsDate = generalSpecification.dateSpecification(startDate, endDate,ApplicationConstants.LAST_MODIFIED_DATE);
		Specification<Employee> containsFirstName = generalSpecification.stringSpecification(firstName,EmployeeConstants.FIRST_NAME);
		Specification<Employee> containsLastName = generalSpecification.stringSpecification(lastName,EmployeeConstants.LAST_NAME);
		Specification<Employee> cardNoSpec = generalSpecification.stringSpecification(cardNo,EmployeeConstants.CARD_ID);
		Specification<Employee> lanyardSpec = generalSpecification.stringSpecification(lanyard,EmployeeConstants.LANYARD);
		Specification<Employee> containsEmployeeId = generalSpecification.stringSpecification(employeeId,EmployeeConstants.EMPLOYEE_ID);
	   	Specification<Employee> departmentSpec = generalSpecification.stringSpecification(department,EmployeeConstants.DEPARTMENT); 
	   	Specification<Employee>  designationSpec = generalSpecification.stringSpecification(designation,EmployeeConstants.DESIGNATION);
	   	Specification<Employee>  employeeTypeSpec = generalSpecification.foreignKeyStringSpecification(employeeType,EmployeeConstants.EMPLOYEE_TYPE,ApplicationConstants.NAME);
	   	Specification<Employee> statusSpc = generalSpecification.stringSpecification(status, EmployeeConstants.STATUS);
		List<Employee> employeeList =employeeRepository.findAll(containsDate.and(containsFirstName).and(containsLastName).and(cardNoSpec).and(lanyardSpec)
				.and(containsEmployeeId).and(departmentSpec).and(designationSpec).and(employeeTypeSpec).and(statusSpc).and(isDeletedFalse));
		return employeeList;
	}
	
	private void excelGenerator(HttpServletResponse response, List<Employee> employeeList) throws ParseException, IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Metal_Exception_Report_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);

		CellStyle cellStyle = exportEmployee.setBorderStyle(workBook, BorderStyle.THICK, font);

		setHeaderForExcel(row, cellStyle);

		font = workBook.createFont();
		font.setBold(false);
		cellStyle = exportEmployee.setBorderStyle(workBook, BorderStyle.THIN, font);
		
		//set data for excel
		setExcelDataCellWise(employeeList, sheet, rowCount, cellStyle);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}

	private void setExcelDataCellWise(List<Employee> employeeList, Sheet sheet, int rowCount, CellStyle cellStyle) {
		for (Employee employee : employeeList) {
			if(rowCount==90000)
				break;
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
			cell.setCellValue(employee.getMetalExceptionName());
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
		cell.setCellValue(HeaderConstants.METAL_EXCEPTION);
		cell.setCellStyle(cellStyle);
		
		
	}

}

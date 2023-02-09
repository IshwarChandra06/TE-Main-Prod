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
public class ExportEmployeeMasterData {

	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Employee> generalSpecification;
	
	@Autowired
	private CalendarUtil calendarUtil;
 	
	public void fileExportBySearchValue(HttpServletResponse response,String sDate,String eDate, String firstName, String lastName,String employeeId,
			String department,String designation,String employeeType,String cardNo, String lanyard,String status,String flag) throws ParseException, IOException {

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
		
		List<Employee> employeeList = getListOfEmployee(firstName,lastName, employeeId, department,
				designation,employeeType,cardNo,lanyard,status,  startDate, endDate);
		
		excelGenerator(response, employeeList);
	}
	public List<Employee> getListOfEmployee(String firstName, String lastName, String employeeId, String department, String designation,
			 String employeeType,String cardNo, String lanyard, String status, Date startDate, Date endDate) {
		Specification<Employee> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		Specification<Employee> containsDate = generalSpecification.dateSpecification(startDate, endDate,ApplicationConstants.LAST_MODIFIED_DATE);
		Specification<Employee> containsFirstName = generalSpecification.stringSpecification(firstName,EmployeeConstants.FIRST_NAME);
		Specification<Employee> containsLastName = generalSpecification.stringSpecification(lastName,EmployeeConstants.LAST_NAME);
		Specification<Employee> containsEmployeeId = generalSpecification.stringSpecification(employeeId,EmployeeConstants.EMPLOYEE_ID);
    	Specification<Employee> departmentSpec = generalSpecification.stringSpecification(department,EmployeeConstants.DEPARTMENT); 
    	Specification<Employee>  designationSpec = generalSpecification.stringSpecification(designation,EmployeeConstants.DESIGNATION);
    	Specification<Employee>  employeeTypeSpec = generalSpecification.foreignKeyStringSpecification(employeeType,EmployeeConstants.EMPLOYEE_TYPE,ApplicationConstants.NAME);
    	Specification<Employee> statusSpc = generalSpecification.stringSpecification(status, EmployeeConstants.STATUS);
    	Specification<Employee> cardNoSpec = generalSpecification.stringSpecification(cardNo,EmployeeConstants.CARD_ID);
		Specification<Employee> lanyardSpec = generalSpecification.stringSpecification(lanyard,EmployeeConstants.LANYARD);
		List<Employee> employeeList =employeeRepository.findAll(containsDate.and(containsFirstName).and(containsLastName).and(lanyardSpec).and(cardNoSpec)
				.and(containsEmployeeId).and(departmentSpec).and(designationSpec).and(employeeTypeSpec).and(statusSpc).and(isDeletedFalse));
		return employeeList;
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
			if(rowCount==90000)
				break;
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
			cell.setCellValue(employee.getLanyardColor());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getManagerId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getManagerName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getManagerEmail());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getHostelName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getHostelWardenName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getHostelWardenEmail());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getHostelWardenMobile());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getBusNo());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getNodalPoint());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getEetoName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(setDateTimeFormat(employee.getLastModifiedDate()));
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getStatus());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getAccesslevels());
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
		cell.setCellValue(HeaderConstants.EMPLOYEE_TYPE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.EIGHT);
		cell.setCellValue(HeaderConstants.CONTACT_NO);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.NINE);
		cell.setCellValue(HeaderConstants.EMAIL);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TEN);
		cell.setCellValue(HeaderConstants.CADRE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.ELEVEN);
		cell.setCellValue(HeaderConstants.PAY_GRADE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWELVE);
		cell.setCellValue(HeaderConstants.JOIN_DATE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.THIRTEEN);
		cell.setCellValue(HeaderConstants.END_DATE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FOURTEEN);
		cell.setCellValue(HeaderConstants.LANYARD);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIFTEEN);
		cell.setCellValue(HeaderConstants.MANAGER_ID);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SIXTEEN);
		cell.setCellValue(HeaderConstants.MANAGER_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SEVENTEEN);
		cell.setCellValue(HeaderConstants.MANAGER_EMAIL);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.EIGHTEEN);
		cell.setCellValue(HeaderConstants.HOSTEL_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.NINETEEN);
		cell.setCellValue(HeaderConstants.HOSTEL_WARDEN_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWENTY);
		cell.setCellValue(HeaderConstants.HOSTEL_WARDEN_EMAIL);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWENTY_ONE);
		cell.setCellValue(HeaderConstants.HOSTEL_WARDEN_MOBILE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWENTY_TWO);
		cell.setCellValue(HeaderConstants.BUS_NO);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWENTY_THREE);
		cell.setCellValue(HeaderConstants.NODAL_POINT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWENTY_FOUR);
		cell.setCellValue(HeaderConstants.EETO_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWENTY_FIVE);
		cell.setCellValue(HeaderConstants.LAST_MODIFIED_DATE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWENTY_SIX);
		cell.setCellValue(HeaderConstants.STATUS);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWENTY_SEVEN);
		cell.setCellValue(HeaderConstants.ACCESS_LEVELS);
		cell.setCellStyle(cellStyle);
		
		
	}
	
}

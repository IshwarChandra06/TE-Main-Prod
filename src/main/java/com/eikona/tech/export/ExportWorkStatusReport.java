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
import com.eikona.tech.constants.DailyAttendanceConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.DailyReport;
import com.eikona.tech.repository.DailyAttendanceRepository;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Component
public class ExportWorkStatusReport {

	
	@Autowired
	private DailyAttendanceRepository dailyAttendanceRepository;
	
	@Autowired
	private GeneralSpecificationUtil<DailyReport> generalSpecification;
	
	@Autowired
	private CalendarUtil calendarUtil;
 
	public void fileExportBySearchValue(HttpServletResponse response, String sDate, String eDate, String employeeName,
			String employeeId, String designation, String department,String employeeType, String workHour, String flag) throws ParseException, IOException {

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
		
		List<DailyReport> dailyAttendanceList = getListOfDailyAttendance(employeeName, employeeId, designation,
				department, startDate, endDate, employeeType, workHour);
		
		excelGenerator(response, dailyAttendanceList);
	}
	
	private List<DailyReport> getListOfDailyAttendance(String employeeName, String employeeId, String designation,
			String department, Date startDate, Date endDate, String employeeType, String workHour) {
		
		Specification<DailyReport> dateSpec = generalSpecification.dateSpecification(startDate, endDate,ApplicationConstants.DATE);
		Specification<DailyReport> employeeNameSpec = generalSpecification.stringSpecification(employeeName,DailyAttendanceConstants.EMPLOYEE_NAME);
		Specification<DailyReport> employeeIdSpec = generalSpecification.stringSpecification(employeeId,DailyAttendanceConstants.EMPLOYEE_ID);
    	Specification<DailyReport> departmentSpec = generalSpecification.stringSpecification(department,DailyAttendanceConstants.DEPARTMENT); 
    	Specification<DailyReport>  designationSpec = generalSpecification.stringSpecification(designation,DailyAttendanceConstants.DESIGNATION);
    	Specification<DailyReport> companySpec = generalSpecification.stringSpecification(employeeType, DailyAttendanceConstants.EMPLOYEE_TYPE);
    	
    	List<DailyReport> dailyAttendanceList = null;
    	if(workHour.isEmpty()) {
    		dailyAttendanceList =dailyAttendanceRepository.findAll(dateSpec.and(employeeNameSpec)
    				.and(employeeIdSpec).and(departmentSpec).and(designationSpec).and(companySpec));
    	}else {
    		Specification<DailyReport> workHourSpec = generalSpecification.stringSpecification("Yes", workHour);
    		
    		dailyAttendanceList =dailyAttendanceRepository.findAll(dateSpec.and(employeeNameSpec)
    				.and(employeeIdSpec).and(departmentSpec).and(designationSpec).and(companySpec).and(workHourSpec));
    	}
		return dailyAttendanceList;
	}
	
	public void excelGenerator(HttpServletResponse response, List<DailyReport> dailyAttendanceList)
			throws ParseException, IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = DailyAttendanceConstants.DAILY_ATTENDANCE_REPORT + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);
		CellStyle cellStyle = setBorderStyle(workBook, BorderStyle.THICK, font);

		//set Header for excel
		setHeaderForExcel(row, cellStyle);

		font = workBook.createFont();
		font.setBold(false);
		cellStyle = setBorderStyle(workBook, BorderStyle.THIN, font);
		
		//set data for excel
		setExcelDataCellWise(dailyAttendanceList, sheet, rowCount, cellStyle);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}
	
	private CellStyle setBorderStyle(Workbook workBook, BorderStyle borderStyle, Font font) {
		CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setBorderTop(borderStyle);
		cellStyle.setBorderBottom(borderStyle);
		cellStyle.setBorderLeft(borderStyle);
		cellStyle.setBorderRight(borderStyle);
		cellStyle.setFont(font);
		return cellStyle;
	}
	
	private void setExcelDataCellWise(List<DailyReport> dailyAttendanceList, Sheet sheet, int rowCount,
			CellStyle cellStyle) {
		for (DailyReport dailyAttendance : dailyAttendanceList) {
			if(rowCount==90000)
				break;
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;

			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getDateStr());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getEmpId());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getEmployeeName());
			cell.setCellStyle(cellStyle);


			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getDepartment());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getDesignation());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getEmployeeType());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getWorkTime());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getLessThanTwo());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getLessThanFour());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getLessThanSix());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getLessThanEight());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(dailyAttendance.getGreaterThanTen());
			cell.setCellStyle(cellStyle);
		}
	}
	
	private void setHeaderForExcel(Row row, CellStyle cellStyle) {
		
		int columnCount = NumberConstants.ZERO;

		Cell cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.DATE);
		cell.setCellStyle(cellStyle);

		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.EMPLOYEE_ID);
		cell.setCellStyle(cellStyle);

		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.EMPLOYEE_NAME);
		cell.setCellStyle(cellStyle);

	    cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.DEPARTMENT);
		cell.setCellStyle(cellStyle);

		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.DESIGNATION);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.EMPLOYEE_TYPE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue(HeaderConstants.WORK_TIME);
		cell.setCellStyle(cellStyle);

		cell = row.createCell(columnCount++);
		cell.setCellValue("Less Than 2 hr");
		cell.setCellStyle(cellStyle);

		cell = row.createCell(columnCount++);
		cell.setCellValue("Less Than 4 hr");
		cell.setCellStyle(cellStyle);

		cell = row.createCell(columnCount++);
		cell.setCellValue("Less Than 6 hr");
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(columnCount++);
		cell.setCellValue("Less Than 8 hr");
		cell.setCellStyle(cellStyle);

		cell = row.createCell(columnCount++);
		cell.setCellValue("More Than 10 hr");
		cell.setCellStyle(cellStyle);

	}
}

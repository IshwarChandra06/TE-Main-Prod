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
import com.eikona.tech.entity.EmployeeShiftInfo;
import com.eikona.tech.repository.EmployeeShiftInfoRepository;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;
@Component
public class ExportEmployeeShiftInfo {
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private GeneralSpecificationUtil<EmployeeShiftInfo> generalSpecification;
	
	@Autowired
	private EmployeeShiftInfoRepository employeeShiftInfoRepository;
	
	public void fileExportBySearchValue(HttpServletResponse response,String sDate, String eDate,
			String employeeId, String employeeName, String department, String shift, String flag) throws ParseException, IOException
			 {
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

		List<EmployeeShiftInfo> employeeShiftList = getEmployeeRosterList( employeeId, employeeName,
				department, shift, startDate, endDate);

		excelGenerator(response, employeeShiftList);
	}

	private List<EmployeeShiftInfo> getEmployeeRosterList(String employeeId, String employeeName,
			String department, String shift, Date startDate, Date endDate) {
		Specification<EmployeeShiftInfo> dateSpc = generalSpecification.dateSpecification(startDate, endDate,ApplicationConstants.DATE);
		Specification<EmployeeShiftInfo> employeeIdSpc = generalSpecification.foreignKeyStringSpecification(employeeId, EmployeeConstants.EMPLOYEE, EmployeeConstants.EMPLOYEE_ID);
		Specification<EmployeeShiftInfo> employeeNameSpc = generalSpecification.foreignKeyStringSpecification(employeeName, EmployeeConstants.EMPLOYEE, EmployeeConstants.FIRST_NAME);
		Specification<EmployeeShiftInfo> departmentSpc = generalSpecification.foreignKeyStringSpecification(department, EmployeeConstants.EMPLOYEE, EmployeeConstants.DEPARTMENT);
		Specification<EmployeeShiftInfo> shiftSpc = generalSpecification.stringSpecification(shift, EmployeeConstants.SHIFT);

		List<EmployeeShiftInfo> employeeShiftList = employeeShiftInfoRepository
				.findAll(dateSpc.and(employeeIdSpc).and(employeeNameSpc).and(departmentSpc).and(shiftSpc));
		return employeeShiftList;
	}

	public void excelGenerator(HttpServletResponse response, List<EmployeeShiftInfo> employeeShiftList)
			throws ParseException, IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = EmployeeConstants.EMPLOYEE_SHIFT + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);

		CellStyle cellStyle = setBorderStyle(workBook, BorderStyle.THICK, font);

		setShiftAssigedExcelHeader(row, cellStyle);

		font = workBook.createFont();
		font.setBold(false);
		cellStyle = setBorderStyle(workBook, BorderStyle.THIN, font);

		setShiftAssignedExcelData(employeeShiftList, sheet, rowCount, cellStyle);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();
		

	}
	
	public String excelGenerator(List<EmployeeShiftInfo> employeeShiftList)
			throws ParseException, IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = EmployeeConstants.EMPLOYEE_SHIFT + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);

		CellStyle cellStyle = setBorderStyle(workBook, BorderStyle.THICK, font);

		setShiftAssigedExcelHeader(row, cellStyle);

		font = workBook.createFont();
		font.setBold(false);
		cellStyle = setBorderStyle(workBook, BorderStyle.THIN, font);

		setShiftAssignedExcelData(employeeShiftList, sheet, rowCount, cellStyle);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		fileOut.close();
		workBook.close();
		
		return filename;

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

	private void setShiftAssignedExcelData(List<EmployeeShiftInfo> employeeShiftList, Sheet sheet,
			int rowCount, CellStyle cellStyle) {
		SimpleDateFormat sdf= new SimpleDateFormat(ApplicationConstants.TIME_FORMAT_24HR);
			for (EmployeeShiftInfo employeeShift : employeeShiftList) {
			if(rowCount==90000)
				break;
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;

			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(employeeShift.getDateStr());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			if (null != employeeShift.getEmployee())
				cell.setCellValue(employeeShift.getEmployee().getEmployeeId());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_SPACE);
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			if (null != employeeShift.getEmployee())
				cell.setCellValue(employeeShift.getEmployee().getFirstName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_SPACE);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if (null != employeeShift.getEmployee())
				cell.setCellValue(employeeShift.getEmployee().getLastName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_SPACE);
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			if (null != employeeShift.getEmployee()) {
				if (null != employeeShift.getEmployee().getDepartment())
					cell.setCellValue(employeeShift.getEmployee().getDepartment());
				else
					cell.setCellValue(ApplicationConstants.DELIMITER_SPACE);
			} else
				cell.setCellValue(ApplicationConstants.DELIMITER_SPACE);
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			if (null != employeeShift.getShift())
				cell.setCellValue(employeeShift.getShift());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_SPACE);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if (null != employeeShift.getWorkScheduleExternalCode())
				cell.setCellValue(employeeShift.getWorkScheduleExternalCode());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_SPACE);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if (null != employeeShift.getStartTime())
				cell.setCellValue(sdf.format(employeeShift.getStartTime()));
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_SPACE);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if (null != employeeShift.getEndTime())
				cell.setCellValue(sdf.format(employeeShift.getEndTime()));
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_SPACE);
			cell.setCellStyle(cellStyle);

		}
	}

	private void setShiftAssigedExcelHeader(Row row, CellStyle cellStyle) {

		Cell cell = row.createCell(NumberConstants.ZERO);
		cell.setCellValue(HeaderConstants.DATE);
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
		cell.setCellValue(HeaderConstants.DEPARTMENT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIVE);
		cell.setCellValue(HeaderConstants.SHIFT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SIX);
		cell.setCellValue(HeaderConstants.WORK_SCHEDULE_EXTERNAL_CODE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SEVEN);
		cell.setCellValue(HeaderConstants.SHIFT_IN_TIME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.EIGHT);
		cell.setCellValue(HeaderConstants.SHIFT_OUT_TIME);
		cell.setCellStyle(cellStyle);
	}
}

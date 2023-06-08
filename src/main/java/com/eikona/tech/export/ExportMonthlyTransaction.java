package com.eikona.tech.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Transaction;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.TransactionRepository;
import com.eikona.tech.util.CalendarUtil;


@Component
@EnableScheduling
public class ExportMonthlyTransaction {
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;
 	
	public String excelGenerator(Calendar calender) {

		try {
			
		String month = calender.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH );
		int last = calender.getActualMaximum(Calendar.DATE);
		
		String currentDateTime = month+"_"+calender.get(Calendar.YEAR);
		String filename = currentDateTime+"_Transaction_Reports" + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);

		CellStyle cellStyle = setBorderStyle(workBook, BorderStyle.THICK, font);
		setHeaderForExcel(row, cellStyle, last, month);

		font = workBook.createFont();
		font.setBold(false);
		cellStyle = setBorderStyle(workBook, BorderStyle.THIN, font);
		
		//set data for excel
		setExcelDataCellWise(sheet,rowCount, cellStyle, last, calender);

		FileOutputStream fileOut = new FileOutputStream(filename);
		
		workBook.write(fileOut);
		fileOut.close();
		workBook.close();
		
		return filename;
		
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

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
	
	private void setExcelDataCellWise(Sheet sheet, int rowCount, CellStyle cellStyle, int last, Calendar calender) {
		
		List<Employee> employeeList = employeeRepository.findAllByIsDeletedFalse();
		for(Employee employee: employeeList) {
			
			if(employee.getId() == 22)
				break;
			
			if(rowCount==90000)
				break;
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;

			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getEmployeeId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getFirstName()+" "+employee.getLastName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getDepartment());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(employee.getDesignation());
			cell.setCellStyle(cellStyle);
			
			int first=1;
			while(first<=last) {
				
				try {
					Date endDate = calendarUtil.getConvertedDate(calender.getTime(), first, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
					Date startDate = calendarUtil.getConvertedDate(calender.getTime(), first, NumberConstants.ZERO, NumberConstants.ZERO, NumberConstants.ZERO);
					
					List<Transaction> transactionList = transactionRepository.findByEmpIdAndDateCustom( employee.getEmployeeId(), startDate, endDate);
					cell = row.createCell(columnCount++);
					StringJoiner deviceTime = new StringJoiner(",");
					for(Transaction tran:transactionList) {
						deviceTime.add(tran.getDeviceName()+"-"+tran.getPunchTimeStr());
					}
					cell.setCellValue(deviceTime.toString());
					cell.setCellStyle(cellStyle);
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				first++;
			}
		}
		
	}
	
    private void setHeaderForExcel(Row row, CellStyle cellStyle, int last, String month) {
		
		int cellIndex=0;
		Cell cell = row.createCell(cellIndex++);
		cell.setCellValue("Employee Id");
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellIndex++);
		cell.setCellValue("Name");
		cell.setCellStyle(cellStyle);

		cell = row.createCell(cellIndex++);
		cell.setCellValue("Department");
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(cellIndex++);
		cell.setCellValue("Function");
		cell.setCellStyle(cellStyle);
		
		int day=1;
		while(day <= last) {
			cell = row.createCell(cellIndex++);
			cell.setCellValue(day+ApplicationConstants.DELIMITER_SPACE+month.substring(NumberConstants.ZERO, NumberConstants.THREE));
			cell.setCellStyle(cellStyle);
			day++;
		}
	}
	
}

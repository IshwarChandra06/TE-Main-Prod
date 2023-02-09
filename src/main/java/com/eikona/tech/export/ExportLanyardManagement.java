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
import com.eikona.tech.constants.CardTrackingConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Lanyard;
import com.eikona.tech.repository.LanyardRepository;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;
@Component
public class ExportLanyardManagement {
	
	@Autowired
	private GeneralSpecificationUtil<Lanyard> generalSpecification;
	
	@Autowired
	private LanyardRepository lanyardRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private ExportEmployeeMasterData exportEmployee;

	public void fileExportBySearchValue(HttpServletResponse response, String sDate, String eDate, String employeeId,
			String type, String status, String flag) throws ParseException, IOException {
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
		Specification<Lanyard> dateSpec = generalSpecification.dateSpecification(startDate, endDate, "issueDate");
		Specification<Lanyard> empSpc = generalSpecification.foreignKeyStringSpecification(employeeId, CardTrackingConstants.EMPLOYEE,EmployeeConstants.EMPLOYEE_ID);
		Specification<Lanyard> typeSpc = generalSpecification.foreignKeyStringSpecification(type, "type","name");
		Specification<Lanyard> statusSpc = generalSpecification.stringSpecification(status, "status");
		
		List<Lanyard> lanyardList = lanyardRepository.findAll(empSpc.and(typeSpc).and(dateSpec).and(statusSpc));
		
		generateExcel(response,lanyardList);
	}

	private void generateExcel(HttpServletResponse response, List<Lanyard> lanyardList)  throws ParseException, IOException {
		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Lanyard_Management_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
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
		setExcelDataCellWise(lanyardList, sheet, rowCount, cellStyle);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}

	private void setExcelDataCellWise(List<Lanyard> lanyardList, Sheet sheet, int rowCount, CellStyle cellStyle) {
		for (Lanyard lanyard : lanyardList) {
			if(rowCount==90000)
				break;
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;
			
			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(lanyard.getEmployee().getEmployeeId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(lanyard.getIssueDateStr());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=lanyard.getEmployee())
			   cell.setCellValue(lanyard.getEmployee().getFirstName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=lanyard.getEmployee())
			 cell.setCellValue(lanyard.getEmployee().getLastName());
			else
			 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=lanyard.getType())
			 cell.setCellValue(lanyard.getType().getName());
			else
			 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			
			cell = row.createCell(columnCount++);
			if(null!=lanyard.getStatus())
			 cell.setCellValue(lanyard.getStatus());
			else
			 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=lanyard.getReason())
			cell.setCellValue(lanyard.getReason());
			else
			cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
		}
		
	}

	private void setHeaderForExcel(Row row, CellStyle cellStyle) {
		
		Cell cell = row.createCell(NumberConstants.ZERO);
		cell.setCellValue(HeaderConstants.EMPLOYEE_ID);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.ONE);
		cell.setCellValue("Issue Date");
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWO);
		cell.setCellValue(HeaderConstants.FIRST_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.THREE);
		cell.setCellValue(HeaderConstants.LAST_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FOUR);
		cell.setCellValue("Type");
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIVE);
		cell.setCellValue("Status");
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SIX);
		cell.setCellValue("Reason");
		cell.setCellStyle(cellStyle);
	}


}

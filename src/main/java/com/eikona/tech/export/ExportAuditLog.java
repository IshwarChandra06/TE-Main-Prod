package com.eikona.tech.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
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
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Audit;
import com.eikona.tech.repository.AuditRepository;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Component
public class ExportAuditLog {
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private AuditRepository auditRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Audit> generalSpecificationUtil;
	
	@Autowired
	private ExportEmployeeMasterData exportEmployee;

	public void fileExportBySearchValue(HttpServletResponse response, String sDate, String eDate, String employeeId,
			String firstName, String lastName, String activity, String flag) throws IOException {
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
		
		Specification<Audit> dateSpec = generalSpecificationUtil.dateSpecification(startDate, endDate, ApplicationConstants.DATE);
		Specification<Audit> employeeIdSpc = generalSpecificationUtil.stringSpecification(employeeId, "employeeId");
		Specification<Audit> firstNameSpec = generalSpecificationUtil.stringSpecification(firstName, "firstName");
		Specification<Audit> lastNameSpc = generalSpecificationUtil.stringSpecification(lastName, "lastName");
		Specification<Audit> activitySpec = generalSpecificationUtil.stringSpecification(activity, "activity");
		
		List<Audit> auditLoglist = auditRepository.findAll(employeeIdSpc.and(dateSpec).and(firstNameSpec).and(activitySpec).and(lastNameSpc));
         generateExcel(response,auditLoglist);
		
	}

	private void generateExcel(HttpServletResponse response, List<Audit> auditLoglist)throws IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Audit_Log_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
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
		setExcelDataCellWise(auditLoglist, sheet, rowCount, cellStyle);
		
		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}

	private void setExcelDataCellWise(List<Audit> auditLoglist, Sheet sheet, int rowCount,
		CellStyle cellStyle) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US);
		for (Audit auditLog : auditLoglist) {
			if(rowCount==90000)
				break;
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;
			
			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(dateFormat.format(auditLog.getDate()));
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(auditLog.getEmployeeId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(auditLog.getFirstName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(auditLog.getLastName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(auditLog.getType());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(auditLog.getActivity());
			cell.setCellStyle(cellStyle);
			
		}
		
	}

	private void setHeaderForExcel(Row row, CellStyle cellStyle) {
		
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
		cell.setCellValue("Type");
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIVE);
		cell.setCellValue("Activity");
		cell.setCellStyle(cellStyle);
	}

}

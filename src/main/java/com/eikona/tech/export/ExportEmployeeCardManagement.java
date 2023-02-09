package com.eikona.tech.export;

import java.io.FileOutputStream;
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
import com.eikona.tech.constants.CardTrackingConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.CardTracking;
import com.eikona.tech.repository.CardTrackingRepository;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Component
public class ExportEmployeeCardManagement {
	
	@Autowired
	private GeneralSpecificationUtil<CardTracking> generalSpecification;
	
	@Autowired
	private CardTrackingRepository cardTrackingRepository;
	
	@Autowired
	private ExportEmployeeMasterData exportEmployee;

	public void fileExportBySearchValue(HttpServletResponse response, String empId, String flag) throws Exception {
    
		Specification<CardTracking> empSpc = generalSpecification.foreignKeyStringSpecification(empId, CardTrackingConstants.EMPLOYEE, EmployeeConstants.EMPLOYEE_ID);
		
    	List<CardTracking> cardTrackingList = cardTrackingRepository.findAll(empSpc);
        generateExcel(response,cardTrackingList);
		
	}

	private void generateExcel(HttpServletResponse response, List<CardTracking> cardManagementList) throws Exception {
			DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
			String currentDateTime = dateFormat.format(new Date());
			String filename = "Employee_Card_Management_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
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
			setExcelDataCellWise(cardManagementList, sheet, rowCount, cellStyle);

			FileOutputStream fileOut = new FileOutputStream(filename);
			workBook.write(fileOut);
			ServletOutputStream outputStream = response.getOutputStream();
			workBook.write(outputStream);
			fileOut.close();
			workBook.close();

		}

		private void setExcelDataCellWise(List<CardTracking> cardManagementList, Sheet sheet, int rowCount, CellStyle cellStyle) {
			for (CardTracking cardTracking : cardManagementList) {
				if(rowCount==90000)
					break;
				Row row = sheet.createRow(rowCount++);

				int columnCount = NumberConstants.ZERO;
				
				Cell cell = row.createCell(columnCount++);
				cell.setCellValue(cardTracking.getCardId());
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(columnCount++);
				cell.setCellValue(cardTracking.getIssueDateStr());
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(columnCount++);
				cell.setCellValue(cardTracking.getEmployee().getEmployeeId());
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(columnCount++);
				if(null!=cardTracking.getEmployee())
				   cell.setCellValue(cardTracking.getEmployee().getFirstName());
				else
					cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(columnCount++);
				if(null!=cardTracking.getEmployee())
				 cell.setCellValue(cardTracking.getEmployee().getLastName());
				else
				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(columnCount++);
				if(null!=cardTracking.getType())
				cell.setCellValue(cardTracking.getType());
				else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(columnCount++);
				if(null!=cardTracking.getDateStr())
				cell.setCellValue(cardTracking.getDateStr());
				else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(columnCount++);
				if(null!=cardTracking.getReason())
				 cell.setCellValue(cardTracking.getReason());
				else
				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
				cell.setCellStyle(cellStyle);
				
			}
			
		}

		private void setHeaderForExcel(Row row, CellStyle cellStyle) {
			
			Cell cell = row.createCell(NumberConstants.ZERO);
			cell.setCellValue(HeaderConstants.CARD_NO);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(NumberConstants.ONE);
			cell.setCellValue("Issue Date");
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(NumberConstants.TWO);
			cell.setCellValue(HeaderConstants.EMPLOYEE_ID);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(NumberConstants.THREE);
			cell.setCellValue(HeaderConstants.FIRST_NAME);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(NumberConstants.FOUR);
			cell.setCellValue(HeaderConstants.LAST_NAME);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(NumberConstants.FIVE);
			cell.setCellValue("Status");
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(NumberConstants.SIX);
			cell.setCellValue("Status Date");
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(NumberConstants.SEVEN);
			cell.setCellValue("Reason");
			cell.setCellStyle(cellStyle);
			
		}

}

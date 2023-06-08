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
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Blacklist;
import com.eikona.tech.repository.BlacklistRepository;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Component
public class ExportSuspensionBlacklist {
	
	@Autowired
	private GeneralSpecificationUtil<Blacklist> generalSpecificationBlacklist;
	
	@Autowired
	private BlacklistRepository blacklistRepository;
	
	@Autowired
	private ExportEmployeeMasterData exportEmployee;
	
	public void fileExportBySearchValue(HttpServletResponse response, String empId, String flag) throws Exception {
		
		Specification<Blacklist> empIdSpc = generalSpecificationBlacklist.foreignKeyStringSpecification(empId, "employee", EmployeeConstants.EMPLOYEE_ID);
		Specification<Blacklist> blacklistSpec = generalSpecificationBlacklist.stringSpecification("Blacklisted", "status");
		Specification<Blacklist> suspendSpec = generalSpecificationBlacklist.stringSpecification("Suspended","status");
		
    	List<Blacklist> blacklistList = blacklistRepository.findAll(empIdSpc.and(blacklistSpec.or(suspendSpec)));
    	 generateExcel(response,blacklistList);
 		
 	}

 	private void generateExcel(HttpServletResponse response, List<Blacklist> blacklistList) throws Exception {
 			DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
 			String currentDateTime = dateFormat.format(new Date());
 			String filename = "Suspension_&_Blacklist_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
 			Workbook workBook = new XSSFWorkbook();
 			Sheet sheet = workBook.createSheet();

 			int rowCount = NumberConstants.ZERO;
 			Row row = sheet.createRow(rowCount++);

 			Font font = workBook.createFont();
 			font.setBold(true);

 			CellStyle cellStyle = exportEmployee.setBorderStyle(workBook, BorderStyle.THICK, font);
 			  setSuspensionHeaderForExcel(row, cellStyle);
 			font = workBook.createFont();
 			font.setBold(false);
 			
 			  cellStyle = exportEmployee.setBorderStyle(workBook, BorderStyle.THIN, font);
 			 
 			//set data for excel
 			   setSuspensionExcelDataCellWise(blacklistList, sheet, rowCount, cellStyle);
 			FileOutputStream fileOut = new FileOutputStream(filename);
 			workBook.write(fileOut);
 			ServletOutputStream outputStream = response.getOutputStream();
 			workBook.write(outputStream);
 			fileOut.close();
 			workBook.close();

 		}

 		private void setSuspensionExcelDataCellWise(List<Blacklist> blacklistList, Sheet sheet, int rowCount, CellStyle cellStyle) {
 			for (Blacklist blacklist : blacklistList) {
 				if(rowCount==90000)
 					break;
 				Row row = sheet.createRow(rowCount++);

 				int columnCount = NumberConstants.ZERO;
 				
 				Cell cell = row.createCell(columnCount++);
 				cell.setCellValue(blacklist.getEmployee().getEmployeeId());
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getEmployee())
 				   cell.setCellValue(blacklist.getEmployee().getFirstName());
 				else
 					cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getEmployee())
 				 cell.setCellValue(blacklist.getEmployee().getLastName());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getStartDateStr())
 				cell.setCellValue(blacklist.getStartDateStr());
 				else
 				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getEndDateStr())
 				cell.setCellValue(blacklist.getEndDateStr());
 				else
 				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getOrderBy())
 				 cell.setCellValue(blacklist.getOrderBy());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getReason())
 				 cell.setCellValue(blacklist.getReason());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getRemovalDate())
 				 cell.setCellValue(blacklist.getRemovalDate());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getReasonForRemoval())
 				 cell.setCellValue(blacklist.getReasonForRemoval());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getStatus())
 				 cell.setCellValue(blacklist.getStatus());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 			}
 			
 		}

 		private void setSuspensionHeaderForExcel(Row row, CellStyle cellStyle) {
 			
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
 			cell.setCellValue("Start Date");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.FOUR);
 			cell.setCellValue("End Date");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.FIVE);
 			cell.setCellValue("Order By");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.SIX);
 			cell.setCellValue("Suspension Reason");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.SEVEN);
 			cell.setCellValue("Whitelist Date");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.EIGHT);
 			cell.setCellValue("Whitelist Reason");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.NINE);
 			cell.setCellValue("Status");
 			cell.setCellStyle(cellStyle);
 			
 		}
 		public String generateSuspensionExcel( List<Blacklist> blacklistList) throws Exception {
 			DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
 			String currentDateTime = dateFormat.format(new Date());
 			String filename = "Suspension_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
 			Workbook workBook = new XSSFWorkbook();
 			Sheet sheet = workBook.createSheet();

 			int rowCount = NumberConstants.ZERO;
 			Row row = sheet.createRow(rowCount++);

 			Font font = workBook.createFont();
 			font.setBold(true);

 			CellStyle cellStyle = exportEmployee.setBorderStyle(workBook, BorderStyle.THICK, font);
 			setSuspensionDataHeaderForExcel(row, cellStyle);
 			font = workBook.createFont();
 			font.setBold(false);
 			
 			  cellStyle = exportEmployee.setBorderStyle(workBook, BorderStyle.THIN, font);
 			 
 			//set data for excel
 			   setSuspensionDataExcelCellWise(blacklistList, sheet, rowCount, cellStyle);
 			FileOutputStream fileOut = new FileOutputStream(filename);
 			workBook.write(fileOut);
 			fileOut.close();
 			workBook.close();
			return filename;

 		}

 		private void setSuspensionDataExcelCellWise(List<Blacklist> blacklistList, Sheet sheet, int rowCount, CellStyle cellStyle) {
 			for (Blacklist blacklist : blacklistList) {
 				if(rowCount==90000)
 					break;
 				Row row = sheet.createRow(rowCount++);

 				int columnCount = NumberConstants.ZERO;
 				
 				Cell cell = row.createCell(columnCount++);
 				cell.setCellValue(blacklist.getEmployee().getEmployeeId());
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getEmployee())
 				   cell.setCellValue(blacklist.getEmployee().getFirstName());
 				else
 					cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getEmployee())
 				 cell.setCellValue(blacklist.getEmployee().getLastName());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getStartDateStr())
 				cell.setCellValue(blacklist.getStartDateStr());
 				else
 				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getEndDateStr())
 				cell.setCellValue(blacklist.getEndDateStr());
 				else
 				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getOrderBy())
 				 cell.setCellValue(blacklist.getOrderBy());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getReason())
 				 cell.setCellValue(blacklist.getReason());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getStatus())
 				 cell.setCellValue(blacklist.getStatus());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 			}
 			
 		}
 		
     private void setSuspensionDataHeaderForExcel(Row row, CellStyle cellStyle) {
 			
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
 			cell.setCellValue("Start Date");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.FOUR);
 			cell.setCellValue("End Date");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.FIVE);
 			cell.setCellValue("Order By");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.SIX);
 			cell.setCellValue("Suspension Reason");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.SEVEN);
 			cell.setCellValue("Status");
 			cell.setCellStyle(cellStyle);
 			
 		}
 		public String generateBlacklistExcel(List<Blacklist> blacklistList) throws Exception {
 			DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
 			String currentDateTime = dateFormat.format(new Date());
 			String filename = "Blacklist_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
 			Workbook workBook = new XSSFWorkbook();
 			Sheet sheet = workBook.createSheet();

 			int rowCount = NumberConstants.ZERO;
 			Row row = sheet.createRow(rowCount++);

 			Font font = workBook.createFont();
 			font.setBold(true);

 			CellStyle cellStyle = exportEmployee.setBorderStyle(workBook, BorderStyle.THICK, font);
 			  setBlacklistHeaderForExcel(row, cellStyle);
 			font = workBook.createFont();
 			font.setBold(false);
 			
 			  cellStyle = exportEmployee.setBorderStyle(workBook, BorderStyle.THIN, font);
 			 
 			//set data for excel
 			 setBlacklistExcelDataCellWise(blacklistList, sheet, rowCount, cellStyle);
 			FileOutputStream fileOut = new FileOutputStream(filename);
 			workBook.write(fileOut);
 			fileOut.close();
 			workBook.close();
			return filename;

 		}
 		
 		private void setBlacklistExcelDataCellWise(List<Blacklist> blacklistList, Sheet sheet, int rowCount, CellStyle cellStyle) {
 			for (Blacklist blacklist : blacklistList) {
 				if(rowCount==90000)
 					break;
 				Row row = sheet.createRow(rowCount++);

 				int columnCount = NumberConstants.ZERO;
 				
 				Cell cell = row.createCell(columnCount++);
 				cell.setCellValue(blacklist.getEmployee().getEmployeeId());
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getEmployee())
 				   cell.setCellValue(blacklist.getEmployee().getFirstName());
 				else
 					cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getEmployee())
 				 cell.setCellValue(blacklist.getEmployee().getLastName());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getStartDateStr())
 				cell.setCellValue(blacklist.getStartDateStr());
 				else
 				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getOrderBy())
 				 cell.setCellValue(blacklist.getOrderBy());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getReason())
 				 cell.setCellValue(blacklist.getReason());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 				
 				cell = row.createCell(columnCount++);
 				if(null!=blacklist.getStatus())
 				 cell.setCellValue(blacklist.getStatus());
 				else
 				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
 				cell.setCellStyle(cellStyle);
 				
 			}
 			
 		}

 		private void setBlacklistHeaderForExcel(Row row, CellStyle cellStyle) {
 			
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
 			cell.setCellValue("Blacklist Date");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.FOUR);
 			cell.setCellValue("Order By");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.FIVE);
 			cell.setCellValue("Blacklist Reason");
 			cell.setCellStyle(cellStyle);
 			
 			cell = row.createCell(NumberConstants.SIX);
 			cell.setCellValue("Status");
 			cell.setCellStyle(cellStyle);
 			
 		}
}

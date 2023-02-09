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
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.repository.AccessLevelRepository;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Component
public class ExportAccessLevel {
	
	@Autowired
	private GeneralSpecificationUtil<AccessLevel> generalSpecification;
	
	@Autowired
	private AccessLevelRepository accessLevelRepository;
	
	@Autowired
	private ExportEmployeeMasterData exportEmployee;

	public void fileExportBySearchValue(HttpServletResponse response, String name, String zone, String building,
			String plant, String flag) throws IOException {
		Specification<AccessLevel> zoneSpc = generalSpecification.foreignKeyStringSpecification(zone, "zone", ApplicationConstants.NAME);
		Specification<AccessLevel> nameSpec = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<AccessLevel> buildingSpc = generalSpecification.foreignKeyStringSpecification(building, "building", ApplicationConstants.NAME);
		Specification<AccessLevel> plantSpc = generalSpecification.foreignKeyDoubleStringSpecification(plant,"building","plant",ApplicationConstants.NAME);
		
		List<AccessLevel> accessLevelList = accessLevelRepository.findAll(zoneSpc.and(nameSpec).and(buildingSpc).and(plantSpc));
        generateExcel(response,accessLevelList);
		
	}

	private void generateExcel(HttpServletResponse response, List<AccessLevel> accessLevelList) throws IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Access_Level_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
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
		setExcelDataCellWise(accessLevelList, sheet, rowCount, cellStyle);
		
		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}

	private void setExcelDataCellWise(List<AccessLevel> accessLevelList, Sheet sheet, int rowCount,
		CellStyle cellStyle) {
		for (AccessLevel accessLevel : accessLevelList) {
			if(rowCount==90000)
				break;
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;
			
			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(accessLevel.getName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=accessLevel.getZone())
			   cell.setCellValue(accessLevel.getZone().getName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=accessLevel.getBuilding())
			 cell.setCellValue(accessLevel.getBuilding().getName());
			else
			 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=accessLevel.getBuilding()) {
				if(null!=accessLevel.getBuilding().getPlant())
					cell.setCellValue(accessLevel.getBuilding().getPlant().getName());
				else
					cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			}
			else
			  cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=accessLevel.getDevices())
			 cell.setCellValue(accessLevel.getDevices());
			else
			 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			
		}
		
	}

	private void setHeaderForExcel(Row row, CellStyle cellStyle) {
		
		Cell cell = row.createCell(NumberConstants.ZERO);
		cell.setCellValue(HeaderConstants.NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.ONE);
		cell.setCellValue(HeaderConstants.ZONE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWO);
		cell.setCellValue(HeaderConstants.BUILDING);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.THREE);
		cell.setCellValue(HeaderConstants.PLANT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FOUR);
		cell.setCellValue(HeaderConstants.DEVICES);
		cell.setCellStyle(cellStyle);
		
	}
		
	}

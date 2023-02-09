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
import com.eikona.tech.entity.Device;
import com.eikona.tech.repository.DeviceRepository;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Component
public class ExportDevice {

	@Autowired
	private GeneralSpecificationUtil<Device> generalSpecification;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private ExportEmployeeMasterData exportEmployee;
	
	public void fileExportBySearchValue(HttpServletResponse response, String name, String zone, String building,
			String plant, String accesslevel, String serialNo, String ipAddress, String status, String flag) throws IOException {
		Specification<Device> zoneSpc = generalSpecification.foreignKeyDoubleStringSpecification(zone,"device", "zone", ApplicationConstants.NAME);
		Specification<Device> nameSpec = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<Device> serialNoSpec = generalSpecification.stringSpecification(serialNo, "serialNo");
		Specification<Device> ipAddressSpec = generalSpecification.stringSpecification(ipAddress, "ipAddress");
		Specification<Device> statusSpc = generalSpecification.stringSpecification(status, "status");
		Specification<Device> buildingSpc = generalSpecification.foreignKeyDoubleStringSpecification(building,"device", "building", ApplicationConstants.NAME);
		Specification<Device> plantSpc = generalSpecification.foreignKeyTripleSpecification(plant,"device","building","plant",ApplicationConstants.NAME);
		Specification<Device> accesslevelSpec = generalSpecification.foreignKeyStringSpecification(accesslevel,"device", ApplicationConstants.NAME);
		List<Device> deviceList = deviceRepository.findAll(zoneSpc.and(nameSpec).and(buildingSpc).and(plantSpc).and(statusSpc).and(accesslevelSpec).and(serialNoSpec).and(ipAddressSpec));
        generateExcel(response,deviceList);
		
	}

	private void generateExcel(HttpServletResponse response, List<Device> deviceList)throws IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Device_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
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
		setExcelDataCellWise(deviceList, sheet, rowCount, cellStyle);
		
		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}

	private void setExcelDataCellWise(List<Device> deviceList, Sheet sheet, int rowCount,
		CellStyle cellStyle) {
		for (Device device : deviceList) {
			if(rowCount==90000)
				break;
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;
			
			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(device.getName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(device.getSerialNo());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(device.getIpAddress());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=device.getAccessLevel()) {
				if(null!=device.getAccessLevel().getBuilding()) {
					if(null!=device.getAccessLevel().getBuilding().getPlant())
						cell.setCellValue(device.getAccessLevel().getBuilding().getPlant().getName());
					else
						cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
				}
				else
					  cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
					
			}
			else
			  cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=device.getAccessLevel()) {
				if(null!=device.getAccessLevel().getBuilding())
					 cell.setCellValue(device.getAccessLevel().getBuilding().getName());
					else
					 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			}
			else
				 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=device.getAccessLevel())
			 cell.setCellValue(device.getAccessLevel().getName());
			else
			 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=device.getAccessLevel()) {
				if(null!=device.getAccessLevel().getZone())
					 cell.setCellValue(device.getAccessLevel().getZone().getName());
					else
					 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			}
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
		cell.setCellValue(HeaderConstants.SERIAL_NO);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWO);
		cell.setCellValue(HeaderConstants.IP_ADDRESS);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.THREE);
		cell.setCellValue(HeaderConstants.PLANT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FOUR);
		cell.setCellValue(HeaderConstants.BUILDING);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIVE);
		cell.setCellValue(HeaderConstants.ACCESS_LEVEL);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SIX);
		cell.setCellValue(HeaderConstants.ZONE);
		cell.setCellStyle(cellStyle);
		
	}

}

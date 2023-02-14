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
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.DeviceHealthStatus;
import com.eikona.tech.repository.DeviceHealthStatusRepository;
import com.eikona.tech.util.CalendarUtil;
@Component
public class ExportOfflineDevice {
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private DeviceHealthStatusRepository deviceHealthStatusRepository;
	
	@Autowired
	private ExportEmployeeMasterData exportEmployee;

	public void fileExportBySearchValue(HttpServletResponse response, Long id, String flag) throws IOException {
			
			Date startDate = calendarUtil.getDateByAddingHour(new Date(),-24);
			
			List<DeviceHealthStatus> deviceHealthStatusList = deviceHealthStatusRepository.findByOfflineDeviceIdAndDateCustom(id, startDate, new Date());
			 generateExcel(response,deviceHealthStatusList);
				
			}


			private void generateExcel(HttpServletResponse response, List<DeviceHealthStatus> deviceHealthStatusList)throws IOException {

				DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
				String currentDateTime = dateFormat.format(new Date());
				String filename = "Device_Offline_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
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
				setExcelDataCellWise(deviceHealthStatusList, sheet, rowCount, cellStyle);
				
				FileOutputStream fileOut = new FileOutputStream(filename);
				workBook.write(fileOut);
				ServletOutputStream outputStream = response.getOutputStream();
				workBook.write(outputStream);
				fileOut.close();
				workBook.close();

			}

			private void setExcelDataCellWise(List<DeviceHealthStatus> deviceHealthStatusList, Sheet sheet, int rowCount,
				CellStyle cellStyle) {
				for (DeviceHealthStatus deviceHealthStatus : deviceHealthStatusList) {
					if(rowCount==90000)
						break;
					Row row = sheet.createRow(rowCount++);

					int columnCount = NumberConstants.ZERO;
					
					Cell cell = row.createCell(columnCount++);
					if(null!=deviceHealthStatus.getDevice()) 
					 cell.setCellValue(deviceHealthStatus.getDevice().getName());
					else
					 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					if(null!=deviceHealthStatus.getDevice()) 
					 cell.setCellValue(deviceHealthStatus.getDevice().getSerialNo());
					else
					 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					if(null!=deviceHealthStatus.getDevice()) 
					 cell.setCellValue(deviceHealthStatus.getDevice().getIpAddress());
					else
					 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					if(null!=deviceHealthStatus.getDevice()) {
						if(null!=deviceHealthStatus.getDevice().getAccessLevel()) {
							if(null!=deviceHealthStatus.getDevice().getAccessLevel().getBuilding()) {
								if(null!=deviceHealthStatus.getDevice().getAccessLevel().getBuilding().getPlant())
									cell.setCellValue(deviceHealthStatus.getDevice().getAccessLevel().getBuilding().getPlant().getName());
								else
									cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
							}
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
					if(null!=deviceHealthStatus.getDevice()) {
						if(null!=deviceHealthStatus.getDevice().getAccessLevel()) {
							if(null!=deviceHealthStatus.getDevice().getAccessLevel().getBuilding())
								 cell.setCellValue(deviceHealthStatus.getDevice().getAccessLevel().getBuilding().getName());
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
					if(null!=deviceHealthStatus.getDevice()) {
						if(null!=deviceHealthStatus.getDevice().getAccessLevel())
							 cell.setCellValue(deviceHealthStatus.getDevice().getAccessLevel().getName());
						else
							 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
					}
					else
						 cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
					
					cell.setCellStyle(cellStyle);
					
					cell = row.createCell(columnCount++);
					if(null!=deviceHealthStatus.getDevice()) {
						if(null!=deviceHealthStatus.getDevice().getAccessLevel()) {
							if(null!=deviceHealthStatus.getDevice().getAccessLevel().getZone())
								 cell.setCellValue(deviceHealthStatus.getDevice().getAccessLevel().getZone().getName());
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
					cell.setCellValue(deviceHealthStatus.getTimeStr());
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
				
				cell = row.createCell(NumberConstants.SEVEN);
				cell.setCellValue(HeaderConstants.TIME);
				cell.setCellStyle(cellStyle);
				
			}

}

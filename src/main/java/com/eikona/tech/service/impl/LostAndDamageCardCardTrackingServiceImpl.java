package com.eikona.tech.service.impl;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.CardTrackingConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.CardTracking;
import com.eikona.tech.repository.CardTrackingRepository;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class LostAndDamageCardCardTrackingServiceImpl {
	
	@Autowired
	private CardTrackingRepository cardTrackingRepository;
	
	@Autowired
	private GeneralSpecificationUtil<CardTracking> generalSpecification;
	
	@Autowired
	private CalendarUtil calendarUtil;

	public PaginationDto<CardTracking> searchByField(String cardId, String employee, String sDate,String eDate, List<String> cardStatus, int pageno, String sortField, String sortDir) {
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
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<CardTracking> page = getCardTrackingPage(cardId, employee, startDate,endDate, cardStatus, pageno, sortField, sortDir);
        List<CardTracking> cardTrackingList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<CardTracking> dtoList = new PaginationDto<CardTracking>(cardTrackingList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	
	}
	
	private Page<CardTracking> getCardTrackingPage(String cardId, String employee, Date startDate,Date endDate, List<String> cardStatus, int pageno, String sortField, String sortDir) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<CardTracking> cardIdSpc = generalSpecification.stringSpecification(cardId, CardTrackingConstants.CARD_ID);
		Specification<CardTracking> empSpc = generalSpecification.foreignKeyStringSpecification(employee, CardTrackingConstants.EMPLOYEE,EmployeeConstants.EMPLOYEE_ID);
		Specification<CardTracking> dateSpc = generalSpecification.dateSpecification(startDate, endDate, ApplicationConstants.DATE);
		Specification<CardTracking> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		
		Specification<CardTracking> typeSpc = generalSpecification.stringSpecification(cardStatus, CardTrackingConstants.TYPE);
		
    	Page<CardTracking> page = cardTrackingRepository.findAll(cardIdSpc.and(empSpc).and(dateSpc).and(typeSpc).and(isDeletedFalse), pageable);
		return page;
	
	}

	public void fileExportBySearchValue(HttpServletResponse response, String cardId, String employee, String sDate, String eDate, List<String> cardStatus, String flag) {
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
		Specification<CardTracking> cardIdSpc = generalSpecification.stringSpecification(cardId, CardTrackingConstants.CARD_ID);
		Specification<CardTracking> empSpc = generalSpecification.foreignKeyStringSpecification(employee, CardTrackingConstants.EMPLOYEE,EmployeeConstants.EMPLOYEE_ID);
		Specification<CardTracking> dateSpc = generalSpecification.dateSpecification(startDate, endDate, ApplicationConstants.DATE);
		Specification<CardTracking> typeSpc = generalSpecification.stringSpecification(cardStatus, CardTrackingConstants.TYPE);
		
		List<CardTracking> damageCardTrackingList = cardTrackingRepository.findAll(cardIdSpc.and(empSpc).and(dateSpc).and(typeSpc));
    	
    	try {
			excelGenerator(response, damageCardTrackingList);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void excelGenerator(HttpServletResponse response, List<CardTracking> cardTrackingList)
			throws ParseException, IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_SPACE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Damage_card_Management_" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);
		CellStyle cellStyle = setBorderStyle(workBook, BorderStyle.THICK, font);

		//set header for excel
		setHeaderForExcel(row, cellStyle);

		font = workBook.createFont();
		font.setBold(false);
		cellStyle = setBorderStyle(workBook, BorderStyle.THIN, font);
		
		//set data for excel
		setExcelDataCellWise(cardTrackingList, sheet, rowCount, cellStyle);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

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
	
	private void setExcelDataCellWise(List<CardTracking> cardTrackingList, Sheet sheet, int rowCount,
			CellStyle cellStyle) {
		
		for (CardTracking cardTracking : cardTrackingList) {
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;

			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(cardTracking.getCardId());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(cardTracking.getIssueDateStr());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue((null != cardTracking.getEmployee())?cardTracking.getEmployee().getEmployeeId():"");
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue((null != cardTracking.getEmployee())?cardTracking.getEmployee().getFirstName():"");
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue((null != cardTracking.getEmployee())?cardTracking.getEmployee().getLastName():"");
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(cardTracking.getType());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(cardTracking.getDateStr());
			cell.setCellStyle(cellStyle);
			
		}
	}
	
	private void setHeaderForExcel(Row row, CellStyle cellStyle) {
		
		Cell cell = row.createCell(NumberConstants.ZERO);
		cell.setCellValue(CardTrackingConstants.CARD_ID_FIELD);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.ONE);
		cell.setCellValue(CardTrackingConstants.ISSUE_DATE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.TWO);
		cell.setCellValue(CardTrackingConstants.EMPLLOYEE_ID);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.THREE);
		cell.setCellValue(CardTrackingConstants.EMPLOYEE_FIRST_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FOUR);
		cell.setCellValue(CardTrackingConstants.EMPLOYEE_LAST_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIVE);
		cell.setCellValue(CardTrackingConstants.CARD_STATUS);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SIX);
		cell.setCellValue(CardTrackingConstants.STATUS_DATE);
		cell.setCellStyle(cellStyle);
		
	}
	
}

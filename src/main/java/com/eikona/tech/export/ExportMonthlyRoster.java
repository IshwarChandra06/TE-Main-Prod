package com.eikona.tech.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import com.eikona.tech.dto.MonthlyReportDto;
import com.eikona.tech.dto.MonthlyShiftDto;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.EmployeeShiftInfo;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.EmployeeShiftInfoRepository;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.GeneralSpecificationUtil;
@Component
public class ExportMonthlyRoster {
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private GeneralSpecificationUtil<EmployeeShiftInfo> generalSpecification;
	
	@Autowired
	private EmployeeShiftInfoRepository employeeShiftInfoRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	public void fileExportBySearchValue(HttpServletResponse response, Long id, String sDate, String eDate,
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

		List<EmployeeShiftInfo> employeeShiftList = getEmployeeRosterList(id, employeeId, employeeName,
				department, shift, startDate, endDate);

		excelGenerator(response, employeeShiftList);
	}

	private List<EmployeeShiftInfo> getEmployeeRosterList(Long id, String employeeId, String employeeName,
			String department, String shift, Date startDate, Date endDate) {
		Specification<EmployeeShiftInfo> idSpc = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<EmployeeShiftInfo> dateSpc = generalSpecification.dateSpecification(startDate, endDate,ApplicationConstants.DATE);
		Specification<EmployeeShiftInfo> employeeIdSpc = generalSpecification.foreignKeyStringSpecification(employeeId, EmployeeConstants.EMPLOYEE, EmployeeConstants.EMPLOYEE_ID);
		Specification<EmployeeShiftInfo> employeeNameSpc = generalSpecification.foreignKeyStringSpecification(employeeName, EmployeeConstants.EMPLOYEE, EmployeeConstants.FIRST_NAME);
		Specification<EmployeeShiftInfo> departmentSpc = generalSpecification.foreignKeyStringSpecification(department, EmployeeConstants.EMPLOYEE, EmployeeConstants.DEPARTMENT);
		Specification<EmployeeShiftInfo> shiftSpc = generalSpecification.stringSpecification(shift, EmployeeConstants.SHIFT);

		List<EmployeeShiftInfo> employeeShiftList = employeeShiftInfoRepository
				.findAll(idSpc.and(dateSpc).and(employeeIdSpc).and(employeeNameSpc).and(departmentSpc).and(shiftSpc));
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
		cell.setCellValue(HeaderConstants.SHIFT_IN_TIME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.SEVEN);
		cell.setCellValue(HeaderConstants.SHIFT_OUT_TIME);
		cell.setCellStyle(cellStyle);
	}
	
	
	public void fileExportBySearchValue(HttpServletResponse response, String dateStr, String employeeId,
			String employeeName, String department, String shift, String flag) throws ParseException, IOException {
		
		MonthlyReportDto<MonthlyShiftDto> monthlyDetailsReport = new MonthlyReportDto<>();
		
		SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		Date startDate = null;
		Date endDate = null;
		if(!dateStr.isEmpty()) {
			dateStr=dateStr+"-01";
			try {
				Date date = format.parse(dateStr);
				
				Calendar dateCalendar = Calendar.getInstance(); 
				dateCalendar.setTime(date);
				String month = dateCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH );
				
				int first = dateCalendar.getActualMinimum(Calendar.DATE);
				int last = dateCalendar.getActualMaximum(Calendar.DATE);
				
				List<String> headList = getHeadList(month, first, last);

				startDate= calendarUtil.setDayInCalendar(date, first, NumberConstants.ZERO, NumberConstants.ZERO, NumberConstants.ZERO);
				
				endDate= calendarUtil.setDayInCalendar(date, last, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
				
				List<Employee> employeeList = (List<Employee>) employeeRepository.findAll();
				
				List<MonthlyShiftDto> monthlyReportList = new ArrayList<>();
				for(Employee employee: employeeList) {
					MonthlyShiftDto monthlyDetailDto = calculateDaywiseMonthlyReport(startDate, endDate, employeeName, department, shift, employee);
					
					if(null != monthlyDetailDto.getEmpId())
						monthlyReportList.add(monthlyDetailDto);
				}
				
				monthlyDetailsReport.setHeadList(headList);
				monthlyDetailsReport.setDataList(monthlyReportList);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		excelGenerator(response, monthlyDetailsReport);
	}
	
	
	public void excelGenerator(HttpServletResponse response, MonthlyReportDto<MonthlyShiftDto> monthlyDetailsList) throws IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_UNDERSCORE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Employee_Shift" + currentDateTime + ApplicationConstants.EXTENSION_EXCEL;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount = NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);

		//set border style for header data
		CellStyle cellStyle = setBorderStyle(workBook, BorderStyle.THICK, font);

		int index=NumberConstants.ZERO;
		Cell cell = row.createCell(NumberConstants.ZERO);
		List<String> headList = monthlyDetailsList.getHeadList();
		for(String head : headList) {
			cell = row.createCell(index++);
			cell.setCellValue(head);
			cell.setCellStyle(cellStyle);
		}
		
		font = workBook.createFont();
		font.setBold(false);

		//set border style for body data
		cellStyle = setBorderStyle(workBook, BorderStyle.THIN, font);

		List<MonthlyShiftDto> incapMonthlyDetailDtoList = monthlyDetailsList.getDataList();
		
		//set excel data for incap monthly report
		setExcelDataForMonthlyReport(sheet, rowCount, cellStyle, incapMonthlyDetailDtoList);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		workBook.write(outputStream);
		fileOut.close();
		workBook.close();

	}
	
	
	private void setExcelDataForMonthlyReport(Sheet sheet, int rowCount, CellStyle cellStyle,
			List<MonthlyShiftDto> monthlyDetailDtoList) {
		
		for (MonthlyShiftDto monthlyDetail : monthlyDetailDtoList) {
			
			if(rowCount==90000)
				break;
			
			Row row = sheet.createRow(rowCount++);

			int columnCount = NumberConstants.ZERO;

			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getEmpId());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getFirstName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getLastName());
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			cell.setCellValue(monthlyDetail.getDepartment());
			cell.setCellStyle(cellStyle);

			//month
			for(String data: monthlyDetail.getDateList()) {
				cell = row.createCell(columnCount++);
				cell.setCellValue(data);
				cell.setCellStyle(cellStyle);
			}
		}
	}
	
	public MonthlyShiftDto calculateDaywiseMonthlyReport(Date startDate, Date endDate,
			String employeeName, String department, String shift, Employee employee){
		 
		Calendar calender = Calendar.getInstance();
		calender.setTime(endDate);
		int lastDayOfMonth = calender.get(Calendar.DATE);
		
		List<EmployeeShiftInfo> employeeShiftInfoList = employeeShiftInfoRepository.findDetailsByDateCustom(employee.getEmployeeId(), startDate, endDate);
		MonthlyShiftDto monthlyDailyReportDto = new MonthlyShiftDto();
		
			//set employee details in monthly report
			setEmployeeDetailsInMonthlyReport(employee, monthlyDailyReportDto);
				
			Iterator<EmployeeShiftInfo> employeeShiftInfoItr = employeeShiftInfoList.iterator();
			EmployeeShiftInfo employeeShiftInfo = null;
			if (employeeShiftInfoItr.hasNext()) {
				employeeShiftInfo = employeeShiftInfoItr.next();
			}
			
			Calendar startDayCalendar = Calendar.getInstance(); 
			startDayCalendar.setTime(startDate);
			int first = startDayCalendar.getActualMinimum(Calendar.DATE);
			
			Calendar endDayCalendar = Calendar.getInstance(); 
			endDayCalendar.setTime(endDate);
			int last = endDayCalendar.getActualMaximum(Calendar.DATE);
			
			//set monthly report data
			setMonthlyReportDto(lastDayOfMonth, monthlyDailyReportDto, employeeShiftInfoItr, employeeShiftInfo, first,
					last);
			
//		}
		
		return monthlyDailyReportDto;
		 
	 }
	
	private void setMonthlyReportDto(int lastDayOfMonth, MonthlyShiftDto monthlyDailyReportDto,
			Iterator<EmployeeShiftInfo> employeeShiftInfoItr, EmployeeShiftInfo employeeShiftInfo, int first, int last) {
		
		List<String> dataList = new ArrayList<String>();
		while(first <= last) {
			if(null != employeeShiftInfo) {
				String[] dateArray=employeeShiftInfo.getDateStr().split("-");
				String date=dateArray[2];
				if(Integer.valueOf(date)==first) {
					
					dataList.add(employeeShiftInfo.getShift());
					if (employeeShiftInfoItr.hasNext()) {
						employeeShiftInfo = employeeShiftInfoItr.next();
					}
				} else 
					dataList.add(ApplicationConstants.DELIMITER_HYPHEN);
			}else{
				dataList.add(ApplicationConstants.DELIMITER_HYPHEN);
			}
			
			first++;
		}
		
		monthlyDailyReportDto.setTotalDays(String.valueOf(lastDayOfMonth));
		
		monthlyDailyReportDto.setDateList(dataList);
	}
	
	
	private void setEmployeeDetailsInMonthlyReport(Employee employee, MonthlyShiftDto monthlyDailyReportDto) {
		if(null != employee) {
			
			if(null != employee.getEmployeeId())
				monthlyDailyReportDto.setEmpId(employee.getEmployeeId());
			else
				monthlyDailyReportDto.setEmpId(ApplicationConstants.DELIMITER_EMPTY);
			
			if(null != employee.getFirstName())
				monthlyDailyReportDto.setFirstName(employee.getFirstName());
			else
				monthlyDailyReportDto.setFirstName(ApplicationConstants.DELIMITER_EMPTY);
			
			if(null != employee.getLastName())
				monthlyDailyReportDto.setLastName(employee.getLastName());
			else
				monthlyDailyReportDto.setLastName(ApplicationConstants.DELIMITER_EMPTY);
			
			if(null != employee.getDepartment())
				monthlyDailyReportDto.setDepartment(employee.getDepartment());
			else
				monthlyDailyReportDto.setDepartment(ApplicationConstants.DELIMITER_EMPTY);
			
		}else {
			monthlyDailyReportDto.setEmpId(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setFirstName(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setLastName(ApplicationConstants.DELIMITER_EMPTY);
			monthlyDailyReportDto.setDepartment(ApplicationConstants.DELIMITER_EMPTY);
		}
	}
	
	private List<String> getHeadList(String month, int day, int last) {
		List<String> headList = new ArrayList<String>();
		headList.add(HeaderConstants.EMPLOYEE_ID);
		headList.add(HeaderConstants.FIRST_NAME);
		headList.add(HeaderConstants.LAST_NAME);
		headList.add(HeaderConstants.DEPARTMENT);
		while(day <= last) {
			headList.add(day+ApplicationConstants.DELIMITER_SPACE+month.substring(NumberConstants.ZERO, NumberConstants.THREE));
			day++;
		}
		return headList;
	}

	

}

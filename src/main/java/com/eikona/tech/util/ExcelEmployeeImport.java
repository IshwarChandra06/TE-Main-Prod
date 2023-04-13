package com.eikona.tech.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.MessageConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.EmployeeType;
import com.eikona.tech.repository.AccessLevelRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.EmployeeTypeRepository;


@Component
public class ExcelEmployeeImport {
	
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private EmployeeTypeRepository employeeTypeRepository;
	
	@Autowired
	private AccessLevelRepository accessLevelRepository;
	
	@Autowired
	private EntityMap entityObjectMap;
	
	
	
	public Employee excelRowToEmployee(Row currentRow,Map<String, AccessLevel> accessLevelMap) {
		
		Employee employeeObj = null;
		
		Iterator<Cell> cellsInRow = currentRow.iterator();
		int cellIndex = NumberConstants.ZERO;
		employeeObj = new Employee();

		

		while (cellsInRow.hasNext()) {
			Cell currentCell = cellsInRow.next();
			cellIndex = currentCell.getColumnIndex();
			if (null == employeeObj) {
				break;
			}

			else if (cellIndex == NumberConstants.ZERO) {
				String str=setString(currentCell);
				employeeObj.setEmployeeId(str);
			} 
			else if (cellIndex == NumberConstants.ONE) {
				setAccessLevel(accessLevelMap,employeeObj, currentCell);
			}
//			else if (cellIndex == NumberConstants.TWO) {
//				setDepartment(employeeObj, currentCell);
//			}
		}
		return employeeObj;
		
	}
//	@SuppressWarnings("deprecation")
//	private void setDepartment(Employee employeeObj, Cell currentCell) {
//		currentCell.setCellType(CellType.STRING);
//		if (currentCell.getCellType() == CellType.NUMERIC) {
//			employeeObj.setDepartment(String.valueOf(currentCell.getNumericCellValue()));
//		} else if (currentCell.getCellType() == CellType.STRING) {
//			employeeObj.setDepartment(currentCell.getStringCellValue());
//		}
//		
//	}
	
	private void setAccessLevel(Map<String, AccessLevel> accessLevelMap,Employee employeeObj, Cell currentCell) {
		String str = currentCell.getStringCellValue();
		if (null != str && !str.isEmpty()) {
			String[] accessZoneList = str.split(ApplicationConstants.DELIMITER_COMMA);
			List<AccessLevel> accessZoneEmployeeList=new ArrayList<AccessLevel>();
			for(String accessZone:accessZoneList) {
				
				AccessLevel accessZoneObj=accessLevelMap.get(accessZone.trim());
				if(null==accessZoneObj) {
					accessZoneObj=new AccessLevel();
					accessZoneObj.setName(accessZone.trim());
					
					accessLevelRepository.save(accessZoneObj);
					accessLevelMap.put(accessZone.trim(), accessZoneObj);
				}
				
				accessZoneEmployeeList.add(accessZoneObj);
			}
			
			employeeObj.setAccessLevel(accessZoneEmployeeList);
		}
	}

	@SuppressWarnings(ApplicationConstants.DEPRECATION)
	private String setString(Cell currentCell) {
		String str="";
		currentCell.setCellType(CellType.STRING);
		if (currentCell.getCellType() == CellType.NUMERIC) {
			str=String.valueOf(currentCell.getNumericCellValue()).trim();
		} else if (currentCell.getCellType() == CellType.STRING) {
			str=currentCell.getStringCellValue().trim();
		}
		return str;
	}

	
	public List<Employee> parseExcelFileAccessLevel(InputStream inputStream) {
		List<Employee> employeeList = new ArrayList<Employee>();
		try {

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			

			int rowNumber = NumberConstants.ZERO;
			
			Map<String, AccessLevel> accMap = entityObjectMap.getAccessLevelByName();
			Map<String, Employee> employeeMap = entityObjectMap.getActiveEmployeeByEmpId();
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;
				
				Employee employee=excelRowToEmployee(currentRow,accMap);
				
				Employee emp=employeeMap.get(employee.getEmployeeId());
				
				 if(null!=emp){
					emp.setAccessLevel(employee.getAccessLevel());
					//emp.setDepartment(employee.getDepartment());
					employeeList.add(emp);
				}
//				 else {
//					employeeList.add(employee);
//				}
					
				
				if(rowNumber%NumberConstants.FIVE_HUNDRED==NumberConstants.ZERO) {
					employeeRepository.saveAll(employeeList);
					employeeList.clear();
				}
					
					
			}
			
			if(!employeeList.isEmpty()) {
				employeeRepository.saveAll(employeeList);
				employeeList.clear();
			}
			
			workbook.close();

			return employeeList;
		} catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	}
	
	public List<Employee> parseExcelFileEmployeeMasterData(InputStream inputStream) {
		List<Employee> employeeList = new ArrayList<Employee>();
		try {

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(NumberConstants.ZERO);

			Iterator<Row> rows = sheet.iterator();

			int rowNumber = NumberConstants.ZERO;
			
			Map<String, EmployeeType> employeeTypeMap = entityObjectMap.getEmployeeTypeByName();
			Map<String, Employee> employeeMap =entityObjectMap.getEmployeeByEmpId();
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == NumberConstants.ZERO) {
					rowNumber++;
					continue;
				}

				rowNumber++;
				
				Employee employee=excelOfEmployeeMasterData(currentRow,employeeTypeMap);
				
				Employee employeeObj = employeeMap.get(employee.getEmployeeId());

				if ((null==employeeObj) && null != employee.getEmployeeId() && !employee.getEmployeeId().isEmpty())
					employeeList.add(employee);
				else if(null!=employeeObj) {
					employeeObj.setCardId(employee.getCardId());
					if(null==employeeObj.getFirstName() || employeeObj.getFirstName().isEmpty())
						employeeObj.setFirstName(employee.getFirstName());
					if(null==employeeObj.getLastName() || employeeObj.getLastName().isEmpty())
						employeeObj.setLastName(employee.getLastName());
					employeeList.add(employeeObj);
				}
					
				
				if(rowNumber%NumberConstants.FIVE_HUNDRED==NumberConstants.ZERO) {
					employeeRepository.saveAll(employeeList);
					employeeList.clear();
				}
					
					
			}
			
			if(!employeeList.isEmpty()) {
				employeeRepository.saveAll(employeeList);
				employeeList.clear();
			}
			
			workbook.close();

			return employeeList;
		} catch (IOException e) {
			throw new RuntimeException(MessageConstants.FAILED_MESSAGE + e.getMessage());
		}
	}
	private Employee excelOfEmployeeMasterData(Row currentRow, Map<String, EmployeeType> employeeTypeMap) {
		
		Employee employeeObj = null;
		
		Iterator<Cell> cellsInRow = currentRow.iterator();
		int cellIndex = NumberConstants.ZERO;
		employeeObj = new Employee();

		while (cellsInRow.hasNext()) {
			Cell currentCell = cellsInRow.next();
			cellIndex = currentCell.getColumnIndex();
			if (null == employeeObj) {
				break;
			}

			else if (cellIndex == NumberConstants.ZERO) {
				String str=setString(currentCell);
				employeeObj.setEmployeeId(str);
				employeeObj.setStatus("Active");
				employeeObj.setSource("Excel");
			} 
			else if (cellIndex == NumberConstants.ONE) {
				String str=setString(currentCell);
				employeeObj.setCardId(str);
			}
			else if (cellIndex == NumberConstants.TWO) {
				String str=setString(currentCell);
				employeeObj.setFirstName(str);
			}
			else if (cellIndex == NumberConstants.THREE) {
				String str=setString(currentCell);
				employeeObj.setLastName(str);
			}
			
			else if (cellIndex == NumberConstants.FOUR) {
				String str=setString(currentCell);
				employeeObj.setDepartment(str);
			}
			else if (cellIndex == NumberConstants.FIVE) {
				String str=setString(currentCell);
				employeeObj.setDesignation(str);
			}
			else if (cellIndex == NumberConstants.SIX) {
				String str=setString(currentCell);
				setEmployeeType(employeeTypeMap,str,employeeObj);
			}
			else if (cellIndex == NumberConstants.SEVEN) {
				String str=setString(currentCell);
				employeeObj.setContactNo(str);
			}
			else if (cellIndex == NumberConstants.EIGHT) {
				String str=setString(currentCell);
				employeeObj.setEmailId(str);
			}
			else if (cellIndex == NumberConstants.NINE) {
				String str=setString(currentCell);
				employeeObj.setCadre(str);
			}
			else if (cellIndex == NumberConstants.TEN) {
				String str=setString(currentCell);
				employeeObj.setPayGrade(str);
			}
			else if (cellIndex == NumberConstants.ELEVEN) {
				Date date=setDate(currentCell);
				employeeObj.setJoinDate(date);
			}
			else if (cellIndex == NumberConstants.TWELVE) {
				Date date=setDate(currentCell);
				employeeObj.setEndDate(date);
			}
			else if (cellIndex == NumberConstants.THIRTEEN) {
				String str=setString(currentCell);
				employeeObj.setLanyardColor(str);
			}
			else if (cellIndex == NumberConstants.FOURTEEN) {
				String str=setString(currentCell);
				employeeObj.setManagerId(str);
			}
			else if (cellIndex == NumberConstants.FIFTEEN) {
				String str=setString(currentCell);
				employeeObj.setManagerName(str);
			}
			else if (cellIndex == NumberConstants.SIXTEEN) {
				String str=setString(currentCell);
				employeeObj.setManagerEmail(str);
			}
			else if (cellIndex == NumberConstants.SEVENTEEN) {
				String str=setString(currentCell);
				employeeObj.setHostelName(str);
			}
			else if (cellIndex == NumberConstants.EIGHTEEN) {
				String str=setString(currentCell);
				employeeObj.setHostelWardenName(str);
			}
			else if (cellIndex == NumberConstants.NINETEEN) {
				String str=setString(currentCell);
				employeeObj.setHostelWardenEmail(str);
			}
			else if (cellIndex == NumberConstants.TWENTY) {
				String str=setString(currentCell);
				employeeObj.setHostelWardenMobile(str);
			}
			else if (cellIndex == NumberConstants.TWENTY_ONE) {
				String str=setString(currentCell);
				employeeObj.setBusNo(str);
			}
			else if (cellIndex == NumberConstants.TWENTY_TWO) {
				String str=setString(currentCell);
				employeeObj.setNodalPoint(str);
			}
			else if (cellIndex == NumberConstants.TWENTY_THREE) {
				String str=setString(currentCell);
				employeeObj.setRelUserId(str);
			}
			
		}
		return employeeObj;
		
	}
	private void setEmployeeType(Map<String, EmployeeType> employeeTypeMap, String str, Employee employeeObj) {
		if (null != str && !str.isEmpty()) {
			EmployeeType employeeType = employeeTypeMap.get(str.trim());
			if(null==employeeType) {
				employeeType = new EmployeeType();
				employeeType.setName(str);
				employeeTypeRepository.save(employeeType);
				employeeTypeMap.put(employeeType.getName(), employeeType);
			}
			employeeObj.setEmployeeType(employeeType);
		}
	}

	private Date setDate(Cell currentCell){
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date =null;
		 if (currentCell.getCellType() == CellType.STRING)
			try {
				date=inputFormat.parse(currentCell.getStringCellValue());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		else 
			 date = (Date)currentCell.getDateCellValue();
		 
		 return date;
		 
	}

	
	
}

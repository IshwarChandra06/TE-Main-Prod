package com.eikona.tech.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.constants.SAPServerConstants;
import com.eikona.tech.constants.ShiftConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.dto.SearchRequestDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Blacklist;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.EmployeeShiftInfo;
import com.eikona.tech.entity.LastSyncStatus;
import com.eikona.tech.repository.BlacklistRepository;
import com.eikona.tech.repository.EmployeeShiftInfoRepository;
import com.eikona.tech.repository.LastSyncStatusRepository;
import com.eikona.tech.service.EmployeeShiftInfoService;
import com.eikona.tech.util.BioSecurityServerUtil;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.EntityMap;
import com.eikona.tech.util.GeneralSpecificationUtil;
import com.eikona.tech.util.RequestExecutionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@EnableScheduling
public class EmployeeShiftInfoServiceImpl implements EmployeeShiftInfoService {

	@Autowired
	private EmployeeShiftInfoRepository employeeShiftInfoRepository;
	
	@Autowired
	private LastSyncStatusRepository lastSyncStatusRepository;

	@Autowired
	private RequestExecutionUtil requestExecutionUtil;
	
	@Autowired
	private EntityMap employeeObjectMap;
	
	@Autowired
	private BioSecurityServerUtil bioSecurityServerUtil;

	@SuppressWarnings("rawtypes")
	@Autowired
	private PaginatedServiceImpl paginatedServiceImpl;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private GeneralSpecificationUtil<EmployeeShiftInfo> generalSpecification;

	@Value("${sap.login.username}")
	private String username;

	@Value("${sap.login.password}")
	private String password;

	
	public void syncEmployeeShiftInfoListFromSAP() {
		try {
			int top = NumberConstants.HUNDRED;
			int skip = NumberConstants.ZERO;
			
			Map<String,Employee> empMap=employeeObjectMap.getEmployeeByEmpId();
			Date currentDate= new Date();
			while (true) {

				JSONArray resultsArray = getEmployeeShiftInfoResponseFromSap(top, skip);

				if (null != resultsArray && !resultsArray.isEmpty()) {

					List<EmployeeShiftInfo> employeeShiftInfoList = new ArrayList<EmployeeShiftInfo>();
					for (int i = NumberConstants.ZERO; i < resultsArray.size(); i++) {
						JSONObject currentObj = (JSONObject) resultsArray.get(i);
						List<EmployeeShiftInfo> employeeShiftList = setEmployeeShiftInfoDetails(currentObj,empMap);
						employeeShiftInfoList.addAll(employeeShiftList);

					}
					skip += top;
					saveEmployeeShiftInfoInDbFromSap(employeeShiftInfoList);
				} else
					break;

			}
			LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate);
			lastSyncStatusRepository.save(lastSyncStatus);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Scheduled(cron = "0 0 0/8 * * ?")
	public void updateEmployeeShiftInfoListFromSAP() {
	try {
		int top = NumberConstants.HUNDRED;
		int skip = NumberConstants.ZERO;
		
		Map<String,Employee> empMap=employeeObjectMap.getEmployeeByEmpId();
		Date currentDate= new Date();
		while (true) {

			JSONArray resultsArray = getEmployeeShiftInfoResponseFromSap(top, skip);

			if (null != resultsArray && !resultsArray.isEmpty()) {

				List<EmployeeShiftInfo> employeeShiftInfoList = new ArrayList<EmployeeShiftInfo>();
				for (int i = NumberConstants.ZERO; i < resultsArray.size(); i++) {
					JSONObject currentObj = (JSONObject) resultsArray.get(i);
					List<EmployeeShiftInfo> employeeShiftList = setEmployeeShiftInfoDetails(currentObj,empMap);
					employeeShiftInfoList.addAll(employeeShiftList);

				}
				skip += top;
				updateEmployeeShiftInfoInDbFromSap(employeeShiftInfoList);
			} else
				break;

		}
		LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate);
		lastSyncStatusRepository.save(lastSyncStatus);

	} catch (Exception e) {
		e.printStackTrace();
	}
}
	private LastSyncStatus setLastSyncStatus(Date currentDate) {
		LastSyncStatus lastSyncStatus = lastSyncStatusRepository.findByActivity("SF Employee Shift Sync");
		if(null!=lastSyncStatus)
			lastSyncStatus.setLastSyncTime(currentDate);
		else {
			    lastSyncStatus = new LastSyncStatus();
				lastSyncStatus.setActivity("SF Employee Shift Sync");
				lastSyncStatus.setLastSyncTime(currentDate);
		}
		return lastSyncStatus;
	}
	private List<EmployeeShiftInfo> setEmployeeShiftInfoDetails(JSONObject currentObj,Map<String,Employee> empMap) throws ParseException {

		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		SimpleDateFormat timeFormat = new SimpleDateFormat(ApplicationConstants.TIME_FORMAT_24HR);
		List<EmployeeShiftInfo> employeeShiftInfoList = new ArrayList<EmployeeShiftInfo>();

		JSONObject workScheduleCodeNavObj = (JSONObject) currentObj.get(SAPServerConstants.WORK_SCHEDULE_CODE_NAV);
		
		if (null != workScheduleCodeNavObj) {
			
			JSONObject workScheduleDayModelObj = (JSONObject) workScheduleCodeNavObj.get(SAPServerConstants.WORK_SCHEDULE_DAY_MODELS);
			if (null != workScheduleDayModelObj) {
				JSONArray resultsArray = (JSONArray) workScheduleDayModelObj.get(SAPServerConstants.RESULTS);

				getShiftAssignResultArray(currentObj, dateFormat, timeFormat, employeeShiftInfoList, resultsArray,empMap);
			}
			
		}

		return employeeShiftInfoList;

	}

	private void getShiftAssignResultArray(JSONObject currentObj, SimpleDateFormat dateFormat,
			SimpleDateFormat timeFormat, List<EmployeeShiftInfo> employeeShiftInfoList, JSONArray resultsArray,Map<String,Employee> empMap)
			throws ParseException {
		
		for (int i = NumberConstants.ZERO; i < resultsArray.size(); i++) {

			EmployeeShiftInfo employeeShift = new EmployeeShiftInfo();

			JSONObject dayWiseCurrentObj = (JSONObject) resultsArray.get(i);

			JSONObject dayModelNavObj = (JSONObject) dayWiseCurrentObj.get(SAPServerConstants.DAY_MODEL_NAV);
			if (null != dayModelNavObj) {
				
				JSONObject segementsObj = (JSONObject) dayModelNavObj.get(SAPServerConstants.SEGMENTS);
				
				if (null != segementsObj) {
					
					JSONArray segementsresultsArray = (JSONArray) segementsObj.get(SAPServerConstants.RESULTS);
					
					if (segementsresultsArray.size() != 0) {
						
						JSONObject segementsFirstObj = (JSONObject) segementsresultsArray.get(0);
						
						if (SAPServerConstants.SCHEDULED_WORKING_TIME.equalsIgnoreCase((String) segementsFirstObj.get(SAPServerConstants.CATEGORY))) {

							setEmployeeShiftInfo(currentObj, dateFormat, timeFormat, employeeShiftInfoList,employeeShift, dayWiseCurrentObj, dayModelNavObj, segementsFirstObj,empMap);
						}

					}
				}

			}
			else if((null == dayModelNavObj) && (SAPServerConstants.OFF.equalsIgnoreCase((String) dayWiseCurrentObj.get(SAPServerConstants.CATEGORY)))){
				setEmployeeShiftInfoForHoliday(currentObj, dateFormat, employeeShift, dayWiseCurrentObj,empMap,employeeShiftInfoList);
				
				
			}

		}
	}

	private void setEmployeeShiftInfoForHoliday(JSONObject currentObj, SimpleDateFormat dateFormat,
			EmployeeShiftInfo employeeShift, JSONObject dayWiseCurrentObj,Map<String,Employee> empMap, List<EmployeeShiftInfo> employeeShiftInfoList) throws ParseException {
		
		String userId=(String) currentObj.get(SAPServerConstants.USER_ID);
		Employee employee = empMap.get(userId);
		
		Calendar calendar = Calendar.getInstance();
		employeeShift.setDay((String) dayWiseCurrentObj.get(SAPServerConstants.DAY));
		calendar.set(Calendar.DAY_OF_YEAR, Integer.valueOf(employeeShift.getDay()));
		employeeShift.setDate(dateFormat.parse(dateFormat.format(calendar.getTime())));
		employeeShift.setDateStr(dateFormat.format(calendar.getTime()));
		
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH)+1;
		String yearMonth=String.valueOf(year)+"-"+String.valueOf(month);
		
		String[] dateParts=employeeShift.getDateStr().split("-");
		
		int monthInt=Integer.valueOf(dateParts[1]);
		String monthStr=String.valueOf(monthInt);
		String yearMonthDate=dateParts[0]+"-"+monthStr;
		
		if((null!=employee) && (yearMonthDate.equalsIgnoreCase(yearMonth))) {
			employeeShift.setEmployee(employee);
			employeeShift.setWorkScheduleExternalCode((String) dayWiseCurrentObj.get(SAPServerConstants.WORK_SCHEDULE_EXTERNAL_CODE));
			employeeShift.setDayModel((String) dayWiseCurrentObj.get(SAPServerConstants.DAY_MODEL));
			employeeShift.setHoliday(true);
			employeeShiftInfoList.add(employeeShift);
		}
		 
	}

	private void setEmployeeShiftInfo(JSONObject currentObj, SimpleDateFormat dateFormat, SimpleDateFormat timeFormat,
			List<EmployeeShiftInfo> employeeShiftInfoList, EmployeeShiftInfo employeeShift,JSONObject dayWiseCurrentObj, JSONObject dayModelNavObj, JSONObject segementsFirstObj,Map<String,Employee> empMap)
			throws ParseException {
		String userId=(String) currentObj.get(SAPServerConstants.USER_ID);
		Employee employee = empMap.get(userId);
		
		Calendar calendar = Calendar.getInstance();
		employeeShift.setDay((String) dayWiseCurrentObj.get(SAPServerConstants.DAY));
		calendar.set(Calendar.DAY_OF_YEAR, Integer.valueOf(employeeShift.getDay()));
		employeeShift.setDate(dateFormat.parse(dateFormat.format(calendar.getTime())));
		employeeShift.setDateStr(dateFormat.format(calendar.getTime()));
		
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH)+1;
		String yearMonth=String.valueOf(year)+"-"+String.valueOf(month);
		
		String[] dateParts=employeeShift.getDateStr().split("-");
		int monthInt=Integer.valueOf(dateParts[1]);
		String monthStr=String.valueOf(monthInt);
		String yearMonthDate=dateParts[0]+"-"+monthStr;
		
		if((null!=employee) && (yearMonthDate.equalsIgnoreCase(yearMonth))) {
					employeeShift.setEmployee(employee);
					
					employeeShift.setWorkScheduleExternalCode((String) dayWiseCurrentObj.get(SAPServerConstants.WORK_SCHEDULE_EXTERNAL_CODE));
					employeeShift.setDayModel((String) dayWiseCurrentObj.get(SAPServerConstants.DAY_MODEL));
					employeeShift.setShift((String) dayModelNavObj.get(SAPServerConstants.EXTERNAL_NAME_DEFAULT_VALUE));
					
					Duration startDuration = Duration.parse((String) segementsFirstObj.get(SAPServerConstants.START_TIME));
					Duration endtDuration = Duration.parse((String) segementsFirstObj.get(SAPServerConstants.END_TIME));
					
					String startTimeInHms=String.format(SAPServerConstants.TIME_FORMAT_CONVERSION,  startDuration.toHours(), 
							startDuration.toMinutesPart(), 
							startDuration.toSecondsPart());
					String endTimeInHms=String.format(SAPServerConstants.TIME_FORMAT_CONVERSION,  endtDuration.toHours(), 
							endtDuration.toMinutesPart(), 
							endtDuration.toSecondsPart());
					
					employeeShift.setStartTime(timeFormat.parse(startTimeInHms));
					employeeShift.setEndTime(timeFormat.parse(endTimeInHms));
					
					employeeShiftInfoList.add(employeeShift);
				
		}
		
	}

	public void saveEmployeeShiftInfoInDbFromSap(List<EmployeeShiftInfo> employeeList) {
		List<EmployeeShiftInfo> savingList = new ArrayList<EmployeeShiftInfo>();
		for (EmployeeShiftInfo employeeShift : employeeList) {
				savingList.add(employeeShift);
		}
		 employeeShiftInfoRepository.saveAll(savingList);
		 savingList.clear();

	}
	
	public void updateEmployeeShiftInfoInDbFromSap(List<EmployeeShiftInfo> employeeList) {
		List<EmployeeShiftInfo> savingList = new ArrayList<EmployeeShiftInfo>();
		for (EmployeeShiftInfo employeeShift : employeeList) {
			EmployeeShiftInfo empShift = employeeShiftInfoRepository
					.findByEmployeeIdAndDateStrCustom(employeeShift.getEmployee().getEmployeeId(), employeeShift.getDateStr());
			if (null == empShift)
				savingList.add(employeeShift);
		}
		 employeeShiftInfoRepository.saveAll(savingList);
		 savingList.clear();

	}

	public JSONArray getEmployeeShiftInfoResponseFromSap(int top, int skip) throws Exception {
		JSONArray resultsArray = new JSONArray();
		
			String myurl = SAPServerConstants.EMPLOYEE_SHIFT_INFO_API.formatted(String.valueOf(top),
					String.valueOf(skip));
			String newurl = myurl.replaceAll(ApplicationConstants.DELIMITER_SPACE,
					ApplicationConstants.DELIMITER_FORMAT_SPACE);
			HttpGet request = getSAPGetRequest(newurl);
			String responeData = requestExecutionUtil.executeHttpGetRequest(request);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONObject firstObj = (JSONObject) jsonResponse.get(SAPServerConstants.D);
			resultsArray = (JSONArray) firstObj.get(SAPServerConstants.RESULTS);
		
		return resultsArray;

	}
	
	public JSONArray updateEmployeeShiftInfoResponseFromSap(int top, int skip) throws Exception {
		JSONArray resultsArray = new JSONArray();
		LastSyncStatus lastSyncStatus=lastSyncStatusRepository.findByActivity("SF Employee Shift Sync");
		if(null!=lastSyncStatus) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US_SEPARATED_BY_T);
			
			String lastSyncTime=dateFormat.format(lastSyncStatus.getLastSyncTime())+"Z";
			String currentTime=dateFormat.format(new Date())+"Z";
			String myurl = SAPServerConstants.EMPLOYEE_SHIFT_INFO_BY_DATE_API.formatted(lastSyncTime,currentTime,String.valueOf(top),
					String.valueOf(skip));
			String newurl = myurl.replaceAll(ApplicationConstants.DELIMITER_SPACE,
					ApplicationConstants.DELIMITER_FORMAT_SPACE);
			HttpGet request = getSAPGetRequest(newurl);
			String responeData = requestExecutionUtil.executeHttpGetRequest(request);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONObject firstObj = (JSONObject) jsonResponse.get(SAPServerConstants.D);
			resultsArray = (JSONArray) firstObj.get(SAPServerConstants.RESULTS);
		}
		
		return resultsArray;

	}

	public HttpGet getSAPGetRequest(String myurl) throws Exception {
		HttpGet request = new HttpGet(myurl);
		request.setHeader(ApplicationConstants.HEADER_CONTENT_TYPE, ApplicationConstants.APPLICATION_JSON);
		String auth = username + ApplicationConstants.DELIMITER_COLON + password;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
		String authHeader = ApplicationConstants.BASIC_AUTH + new String(encodedAuth);
		request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		return request;

	}

	
	

	@Override
	public PaginationDto<EmployeeShiftInfo> searchByField(String sDate, String eDate,
			String employeeId, String employeeName, String department, String shift, int pageno, String sortField,
			String sortDir) {
		Date startDate = null;
		Date endDate = null;
		if (!sDate.isEmpty() && !eDate.isEmpty()) {
			SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
			try {

				startDate = format.parse(sDate);
				endDate = format.parse(eDate);
				endDate = calendarUtil.getConvertedDate(endDate, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE,  NumberConstants.FIFTY_NINE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<EmployeeShiftInfo> page = getEmployeeShiftAssignedPage(employeeId, employeeName,
				department, shift, pageno, sortField, sortDir, startDate, endDate);
        List<EmployeeShiftInfo> employeeShiftList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<EmployeeShiftInfo> dtoList = new PaginationDto<EmployeeShiftInfo>(employeeShiftList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}


	private Page<EmployeeShiftInfo> getEmployeeShiftAssignedPage(String employeeId,
			String employeeName, String department, String shift, int pageno, String sortField, String sortDir,
			Date startDate, Date endDate) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
    	Specification<EmployeeShiftInfo> dateSpc = generalSpecification.dateSpecification(startDate,endDate,EmployeeConstants.DATE);
    	Specification<EmployeeShiftInfo> employeeIdSpc = generalSpecification.foreignKeyStringSpecification(employeeId,EmployeeConstants.EMPLOYEE,EmployeeConstants.EMPLOYEE_ID);
    	Specification<EmployeeShiftInfo> employeeNameSpc = generalSpecification.foreignKeyStringSpecification(employeeName,EmployeeConstants.EMPLOYEE,EmployeeConstants.FIRST_NAME); 
    	Specification<EmployeeShiftInfo> departmentSpc = generalSpecification.foreignKeyStringSpecification(department,EmployeeConstants.EMPLOYEE,EmployeeConstants.DEPARTMENT); 
    	Specification<EmployeeShiftInfo>  shiftSpc = generalSpecification.stringSpecification(shift, EmployeeConstants.SHIFT);
		
    	Page<EmployeeShiftInfo> page = employeeShiftInfoRepository.findAll(dateSpc.and(employeeIdSpc).and(employeeNameSpc).and(departmentSpc).and(shiftSpc),pageable);
		return page;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Page<EmployeeShiftInfo> searchByField(int pageNo, int pageSize, String sortField, String sortDirection,
			SearchRequestDto paginatedDto, Principal principal) {

		ObjectMapper oMapper = new ObjectMapper();

		Map<String, String> map = oMapper.convertValue(paginatedDto.getSearchData(), Map.class);

		Date startDate = null;
		Date endDate = null;

		if ((null != map.get("startDate")) && (null != map.get("endDate"))) {
			SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US_WITH_MILLISECOND);
			try {
				startDate = format.parse(map.get("startDate"));
				endDate = format.parse(map.get("endDate"));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (null == sortDirection || sortDirection.isEmpty()) {
			sortDirection = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField =  ApplicationConstants.ID;
		}
		Page<EmployeeShiftInfo> allEmployeeShiftInfo = getEmployeeShiftInfoPage(pageNo, pageSize, sortField,
				sortDirection, map, startDate, endDate);

		return allEmployeeShiftInfo;
	}
	@SuppressWarnings("unchecked")
	private Page<EmployeeShiftInfo> getEmployeeShiftInfoPage(int pageNo, int pageSize, String sortField,
			String sortDirection, Map<String, String> map, Date startDate, Date endDate) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

		Specification<EmployeeShiftInfo> lastModifiedDate = dateSpecification(startDate, endDate, "lastModifiedDate");
		Specification<EmployeeShiftInfo> date = paginatedServiceImpl.genericSpecification("dateStr", map.get("date"));
		Specification<EmployeeShiftInfo> employeeId = paginatedServiceImpl.genericSpecification("employee","employeeId",
				map.get("employeeId"));
		Specification<EmployeeShiftInfo> shiftName = paginatedServiceImpl.genericSpecification("shift",
				map.get("shift"));

		Page<EmployeeShiftInfo> allEmployeeShiftInfo = employeeShiftInfoRepository
				.findAll(lastModifiedDate.and(shiftName).and(date).and(employeeId), pageable);
		return allEmployeeShiftInfo;
	}

	@Override
	public List<EmployeeShiftInfo> findAll() {

		return (List<EmployeeShiftInfo>) employeeShiftInfoRepository.findAll();
	}

	public Specification<EmployeeShiftInfo> dateSpecification(Date startDate, Date endDate, String field) {
		return (root, query, cb) -> {
			if (null == startDate && null == endDate) {
				return cb.conjunction();
			}
			return cb.between(root.<Date>get(field), startDate, endDate);
		};
	}
/********************************************************************Employee Shift Assignment*********************************************************/	
	
//	@Scheduled(cron="0 0 3 * * *")
	public void addAccessLevelToEmployeeFor1stShift() {
		addShiftToBiosecurity(ShiftConstants.FIRST_SHIFT);
	}
//    @Scheduled(cron="0 0 10 * * *")
	public void addAccessLevelToEmployeeFor2ndShift() {
		addShiftToBiosecurity(ShiftConstants.SECOND_SHIFT);
	}
//	@Scheduled(cron="0 0 18 * * *")
	public void addAccessLevelToEmployeeFor3rdShift() {
		addShiftToBiosecurity(ShiftConstants.THIRD_SHIFT);
	}
	private void addShiftToBiosecurity(String shift)  {
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		String date=dateFormat.format(new Date());
		List<EmployeeShiftInfo> employeeShiftList=employeeShiftInfoRepository.findByShiftAndDateStr(shift,date);
		for(EmployeeShiftInfo employeeShiftInfo:employeeShiftList) {
			 if(null!=employeeShiftInfo.getStartTime() && null!=employeeShiftInfo.getEndTime()){ 
				setShiftTimeToEmployee( date, employeeShiftInfo.getEmployee(),employeeShiftInfo.getStartTime(),employeeShiftInfo.getEndTime());
			}
			
		}
	}
	
	@Autowired
	private BlacklistRepository blacklistRepository;
	
	private void setShiftTimeToEmployee(String today, Employee employee,Date startTime,Date endTime) {
		SimpleDateFormat timeFormat = new SimpleDateFormat(ApplicationConstants.TIME_FORMAT_24HR);
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		if("Suspended".equalsIgnoreCase(employee.getStatus())) {
			try {
				String dateStr = dateFormat.format(new Date());
				Date todayDate = dateFormat.parse(dateStr);
				Blacklist blacklist=blacklistRepository.findByEmployeeAndDateCustom(employee.getEmployeeId(),todayDate);
				if(null!=blacklist && !blacklist.getRemovalDate().isEmpty()) {
					today=blacklist.getRemovalDate();
				}
			} 
			catch (ParseException e) {
				e.printStackTrace();
			}
		}
			String stime=today+" "+timeFormat.format(startTime);
			String etime=today+" "+timeFormat.format(endTime);
			bioSecurityServerUtil.addShiftTimeAndAccessLevelToPerson(employee, stime, etime);
			
		
	}
	
	/********************************************************************Employee AccessLevel Removal*********************************************************/

//	@Scheduled(cron="0 45 14 * * *")
	public void removeEntryAccessLevelFor1stShift() {
		removeEntryAccessLevel(ShiftConstants.FIRST_SHIFT);
	}
//    @Scheduled(cron="0 0 23 * * *")
	public void removeEntryAccessLevelFor2ndShift() {
		removeEntryAccessLevel(ShiftConstants.SECOND_SHIFT);
	}
//	@Scheduled(cron="0 15 7 * * *")
	public void removeEntryAccessLevelFor3rdShift() {
		removeEntryAccessLevel(ShiftConstants.THIRD_SHIFT);
	}
	
	public void removeEntryAccessLevel(String shift) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
			String today=dateFormat.format(new Date());
			List<EmployeeShiftInfo> employeeShiftList=employeeShiftInfoRepository.findByShiftAndDateStr(shift,today);
			for(EmployeeShiftInfo employeeShiftInfo:employeeShiftList) {
				if(("Active".equalsIgnoreCase(employeeShiftInfo.getEmployee().getStatus()))) {
					List<AccessLevel> accLevelList=employeeShiftInfo.getEmployee().getAccessLevel();
					List<AccessLevel> accessLevelList= new ArrayList<>();
					for(AccessLevel accLevel:accLevelList) {
						if((accLevel.getName().toLowerCase()).contains("exit")) 
							accessLevelList.add(accLevel);
					}
					employeeShiftInfo.getEmployee().setAccessLevel(accessLevelList);
					bioSecurityServerUtil.addEmployeeToBioSecurity(employeeShiftInfo.getEmployee());
				}
					
				 
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public Date getNextDate(Date date, int day, int hr, int min, int sec) {
		Calendar calender = Calendar.getInstance();
		calender.setTime(date);
		calender.add(Calendar.DATE, day);
		calender.set(Calendar.HOUR, hr);
		calender.set(Calendar.MINUTE, min);
		calender.set(Calendar.SECOND, sec);
		
		return calender.getTime();
	}
	@Override
	public PaginationDto<EmployeeShiftInfo> searchByField(String dateStr, String employeeId, String employeeName,
			String department, String shift, int pageno, String sortField, String sortDir) {
			
			SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
			Date startDate = null;
			Date endDate = null;
			if(!dateStr.isEmpty()) {
				dateStr=dateStr+"-01";
				try {
					Date date = format.parse(dateStr);
					
					Calendar dateCalendar = Calendar.getInstance(); 
					dateCalendar.setTime(date);
					
					int first = dateCalendar.getActualMinimum(Calendar.DATE);
					int last = dateCalendar.getActualMaximum(Calendar.DATE);

					startDate= calendarUtil.setDayInCalendar(date, first, NumberConstants.ZERO, NumberConstants.ZERO, NumberConstants.ZERO);
					
					endDate= calendarUtil.setDayInCalendar(date, last, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (null == sortDir || sortDir.isEmpty()) {
				sortDir = ApplicationConstants.ASC;
			}
			if (null == sortField || sortField.isEmpty()) {
				sortField = ApplicationConstants.ID;
			}
			Page<EmployeeShiftInfo> page = getEmployeeShiftAssignedPage(startDate, endDate, employeeId, employeeName,
					department, shift, pageno, sortField, sortDir);
	        
			List<EmployeeShiftInfo> employeeShiftList =  page.getContent();
			
			sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
			PaginationDto<EmployeeShiftInfo> dtoList = new PaginationDto<EmployeeShiftInfo>(employeeShiftList, page.getTotalPages(),
					page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
			return dtoList;
		}
	private Page<EmployeeShiftInfo> getEmployeeShiftAssignedPage(Date startDate, Date endDate, String employeeId,
			String employeeName, String department, String shift, int pageno, String sortField, String sortDir) {
				

				Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
						: Sort.by(sortField).descending();

				Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);

		    	Specification<EmployeeShiftInfo> dateSpc = generalSpecification.dateSpecification(startDate,endDate,EmployeeConstants.DATE);
		    	Specification<EmployeeShiftInfo> employeeIdSpc = generalSpecification.foreignKeyStringSpecification(employeeId,EmployeeConstants.EMPLOYEE,EmployeeConstants.EMPLOYEE_ID);
		    	Specification<EmployeeShiftInfo> employeeNameSpc = generalSpecification.foreignKeyStringSpecification(employeeName,EmployeeConstants.EMPLOYEE,EmployeeConstants.FIRST_NAME); 
		    	Specification<EmployeeShiftInfo> departmentSpc = generalSpecification.foreignKeyStringSpecification(department,EmployeeConstants.EMPLOYEE,EmployeeConstants.DEPARTMENT); 
		    	Specification<EmployeeShiftInfo>  shiftSpc = generalSpecification.stringSpecification(shift, EmployeeConstants.SHIFT);

		    	Page<EmployeeShiftInfo> page = employeeShiftInfoRepository.findAll(dateSpc.and(employeeIdSpc).and(employeeNameSpc).and(departmentSpc).and(shiftSpc),pageable);
				return page;
			
			}

}

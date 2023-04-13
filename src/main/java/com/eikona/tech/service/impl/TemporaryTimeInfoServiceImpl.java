package com.eikona.tech.service.impl;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
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
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.constants.SAPServerConstants;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.EmployeeShiftInfo;
import com.eikona.tech.entity.LastSyncStatus;
import com.eikona.tech.entity.TemporaryTimeInfo;
import com.eikona.tech.repository.EmployeeShiftInfoRepository;
import com.eikona.tech.repository.LastSyncStatusRepository;
import com.eikona.tech.repository.TemporaryTimeInfoRepository;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.EntityMap;
import com.eikona.tech.util.RequestExecutionUtil;
@Service
@EnableScheduling
public class TemporaryTimeInfoServiceImpl {


	@Autowired
	private TemporaryTimeInfoRepository temporaryTimeInfoRepository;
	
	@Autowired
	private EmployeeShiftInfoRepository employeeShiftInfoRepository;
	
	@Autowired
	private LastSyncStatusRepository lastSyncStatusRepository;
	
	@Autowired
	private RequestExecutionUtil requestExecutionUtil;
	
	@Autowired
	private EntityMap employeeObjectMap;
	
	@Autowired
	private CalendarUtil calendarUtil;


	@Value("${sap.login.username}")
	private String username;

	@Value("${sap.login.password}")
	private String password;
	
	@Scheduled(cron = "0 0 0/8 * * ?")
	public void syncTemporaryTimeInfoListFromSAP() {
		try {
			int top = NumberConstants.HUNDRED;
			int skip = NumberConstants.ZERO;
			
			Map<String,Employee> empMap=employeeObjectMap.getEmployeeByEmpId();
			Date currentDate= new Date();
			while (true) {

				JSONArray resultsArray = getTemporaryTimeInfoResponseFromSap(top, skip);

				List<TemporaryTimeInfo> tempTimeInfoList = new ArrayList<TemporaryTimeInfo>();
				if (null != resultsArray && !resultsArray.isEmpty()) {

					for (int i = NumberConstants.ZERO; i < resultsArray.size(); i++) {
						JSONObject currentObj = (JSONObject) resultsArray.get(i);
						List<TemporaryTimeInfo> tempTimeInfolist = setTemporaryTimeInfoDetails(currentObj,empMap);
					
						tempTimeInfoList.addAll(tempTimeInfolist);
					}
					skip += top;
					saveTemporaryTimeInfoInDbFromSap(tempTimeInfoList);
				} else
					break;

				updateEmployeeShiftInfoFromTempTimeInfo(tempTimeInfoList);
			}
			LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate);
			lastSyncStatusRepository.save(lastSyncStatus);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveTemporaryTimeInfoInDbFromSap(List<TemporaryTimeInfo> tempTimeInfo) {
		List<TemporaryTimeInfo> savingList = new ArrayList<TemporaryTimeInfo>();
		for (TemporaryTimeInfo temporaryTime : tempTimeInfo) {
			TemporaryTimeInfo tempShift = temporaryTimeInfoRepository.findByEmployeeIdAndDateStrCustom(temporaryTime.getEmployee().getEmployeeId(), temporaryTime.getDateStr());
			if (null==tempShift)
				savingList.add(temporaryTime);
			else {
				tempShift.setDate(temporaryTime.getDate());
				tempShift.setDay(temporaryTime.getDay());
				tempShift.setDateStr(temporaryTime.getDateStr());
				tempShift.setDayModel(temporaryTime.getDayModel());
				tempShift.setEndTime(temporaryTime.getEndTime());
				tempShift.setShift(temporaryTime.getShift());
				tempShift.setStartTime(temporaryTime.getStartTime());
				tempShift.setHoliday(temporaryTime.isHoliday());
				tempShift.setWorkScheduleExternalCode(temporaryTime.getWorkScheduleExternalCode());
				savingList.add(tempShift);
			}
				
		}
		 temporaryTimeInfoRepository.saveAll(savingList);

	}
	
	public void updateEmployeeShiftInfoFromTempTimeInfo(List<TemporaryTimeInfo> tempTimeInfo) {
		
		List<EmployeeShiftInfo> savingList = new ArrayList<EmployeeShiftInfo>();
		for (TemporaryTimeInfo temporaryTime : tempTimeInfo) {
			EmployeeShiftInfo empShift = employeeShiftInfoRepository.findByEmployeeIdAndDateStrCustom(temporaryTime.getEmployee().getEmployeeId(), temporaryTime.getDateStr());
				if(null==empShift) {
					empShift= new EmployeeShiftInfo();
				}
				empShift.setDate(temporaryTime.getDate());
				empShift.setDateStr(temporaryTime.getDateStr());
				empShift.setDayModel(temporaryTime.getDayModel());
				empShift.setEmployee(temporaryTime.getEmployee());
				empShift.setEndTime(temporaryTime.getEndTime());
				empShift.setShift(temporaryTime.getShift());
				empShift.setStartTime(temporaryTime.getStartTime());
				empShift.setHoliday(temporaryTime.isHoliday());
				empShift.setWorkScheduleExternalCode(temporaryTime.getWorkScheduleExternalCode());
				savingList.add(empShift);
			
		}
		employeeShiftInfoRepository.saveAll(savingList);

	}
	
	private List<TemporaryTimeInfo> setTemporaryTimeInfoDetails(JSONObject currentObj,Map<String,Employee> empMap) throws ParseException {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		SimpleDateFormat timeFormat = new SimpleDateFormat(ApplicationConstants.TIME_FORMAT_24HR);
		List<TemporaryTimeInfo> temporaryTimeInfoList = new ArrayList<TemporaryTimeInfo>();
		JSONObject workScheduleNavObj = (JSONObject) currentObj.get(SAPServerConstants.WORK_SCHEDULE_NAV);
		
		
		if (null != workScheduleNavObj) {
			
			JSONObject workScheduleDayModelObj = (JSONObject) workScheduleNavObj.get(SAPServerConstants.WORK_SCHEDULE_DAY_MODELS);
			if (null != workScheduleDayModelObj) {
				
				JSONArray resultsArray = (JSONArray) workScheduleDayModelObj.get(SAPServerConstants.RESULTS);

				for (int i = NumberConstants.ZERO; i < resultsArray.size(); i++) {

					TemporaryTimeInfo tempTimeInfo = new TemporaryTimeInfo();

					JSONObject dayWiseCurrentObj = (JSONObject) resultsArray.get(i);

					JSONObject dayModelNavObj = (JSONObject) dayWiseCurrentObj.get(SAPServerConstants.DAY_MODEL_NAV);
					if (null != dayModelNavObj) {
						
						JSONObject segementsObj = (JSONObject) dayModelNavObj.get(SAPServerConstants.SEGMENTS);
						
						if (null != segementsObj) {
							
							JSONArray segementsresultsArray = (JSONArray) segementsObj.get(SAPServerConstants.RESULTS);
							
							if (segementsresultsArray.size() != 0) { 
								
								JSONObject segementsFirstObj = (JSONObject) segementsresultsArray.get(0);
								
								if (SAPServerConstants.SCHEDULED_WORKING_TIME.equalsIgnoreCase((String) segementsFirstObj.get(SAPServerConstants.CATEGORY))) {

									setTemporaryTimeInfo(currentObj, dateFormat, timeFormat, temporaryTimeInfoList,tempTimeInfo, dayWiseCurrentObj, dayModelNavObj, segementsFirstObj,empMap);
								}

							}
						}

					}
					else if((null == dayModelNavObj) && (SAPServerConstants.OFF.equalsIgnoreCase((String) dayWiseCurrentObj.get(SAPServerConstants.CATEGORY)))){
						setTemporaryTimeInfoForHoliday(currentObj, dateFormat, tempTimeInfo, dayWiseCurrentObj,empMap,temporaryTimeInfoList);
						
						
						
					}

				}
			
			}
		}
		
		return temporaryTimeInfoList;
	}
	
	private void setTemporaryTimeInfo(JSONObject currentObj, SimpleDateFormat dateFormat, SimpleDateFormat timeFormat,
			List<TemporaryTimeInfo> temporaryTimeInfoList, TemporaryTimeInfo tempTimeInfo, JSONObject dayWiseCurrentObj,
			JSONObject dayModelNavObj, JSONObject segementsFirstObj,Map<String,Employee> empMap) throws ParseException {
		
		
		String userId=(String) currentObj.get(SAPServerConstants.USER_ID);
		Employee employee = empMap.get(userId);
		if(null!=employee) {
			tempTimeInfo.setEmployee(employee);
			tempTimeInfo.setWorkScheduleExternalCode((String) dayWiseCurrentObj.get(SAPServerConstants.WORK_SCHEDULE_EXTERNAL_CODE));
			tempTimeInfo.setDay((String) dayWiseCurrentObj.get(SAPServerConstants.DAY));
			tempTimeInfo.setDayModel((String) dayWiseCurrentObj.get(SAPServerConstants.DAY_MODEL));


			String dateStr = (String) currentObj.get(SAPServerConstants.START_DATE);
			if(null!=dateStr) {
				Date date = new Date(Long.valueOf(dateStr.substring(NumberConstants.SIX, NumberConstants.NINETEEN)));
				tempTimeInfo.setDate(dateFormat.parse(dateFormat.format(date)));
				tempTimeInfo.setDateStr(dateFormat.format(date));
			}
			 

			tempTimeInfo.setShift((String) dayModelNavObj.get(SAPServerConstants.EXTERNAL_NAME_DEFAULT_VALUE));
			
			Duration startDuration = Duration.parse((String) segementsFirstObj.get(SAPServerConstants.START_TIME));
			Duration endtDuration = Duration.parse((String) segementsFirstObj.get(SAPServerConstants.END_TIME));
			
			String startTimeInHms=String.format(SAPServerConstants.TIME_FORMAT_CONVERSION,  startDuration.toHours(), 
					startDuration.toMinutesPart(), 
					startDuration.toSecondsPart());
			String endTimeInHms=String.format(SAPServerConstants.TIME_FORMAT_CONVERSION,  endtDuration.toHours(), 
					endtDuration.toMinutesPart(), 
					endtDuration.toSecondsPart());
			
			tempTimeInfo.setStartTime(timeFormat.parse(startTimeInHms));
			tempTimeInfo.setEndTime(timeFormat.parse(endTimeInHms));
			
			temporaryTimeInfoList.add(tempTimeInfo);
		}
			
		
	}

	private void setTemporaryTimeInfoForHoliday(JSONObject currentObj, SimpleDateFormat dateFormat,
			TemporaryTimeInfo tempTimeInfo, JSONObject dayWiseCurrentObj,Map<String,Employee> empMap, List<TemporaryTimeInfo> temporaryTimeInfoList) throws ParseException {
		
		String userId=(String) currentObj.get(SAPServerConstants.USER_ID);
		Employee employee = empMap.get(userId);
		if(null!=employee) {
			tempTimeInfo.setEmployee(employee);
			
			tempTimeInfo.setWorkScheduleExternalCode((String) dayWiseCurrentObj.get(SAPServerConstants.WORK_SCHEDULE_EXTERNAL_CODE));
			tempTimeInfo.setDay((String) dayWiseCurrentObj.get(SAPServerConstants.DAY));
			tempTimeInfo.setDayModel((String) dayWiseCurrentObj.get(SAPServerConstants.DAY_MODEL));

			String dateStr = (String) currentObj.get(SAPServerConstants.START_DATE);
			if(null!=dateStr) {
				Date date = new Date(Long.valueOf(dateStr.substring(NumberConstants.SIX, NumberConstants.NINETEEN)));
				tempTimeInfo.setDate(dateFormat.parse(dateFormat.format(date)));
				tempTimeInfo.setDateStr(dateFormat.format(date));
			}
			tempTimeInfo.setHoliday(true);
			temporaryTimeInfoList.add(tempTimeInfo);
		}
		
	}

	private JSONArray getTemporaryTimeInfoResponseFromSap(int top, int skip) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US_SEPARATED_BY_T);
		String startTime=null;
		String endTime=null;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String dateStr = format.format(new Date());
			Date startDate=calendarUtil.getConvertedDate(format.parse(dateStr), 00, 00, 00);
			startTime=dateFormat.format(startDate)+"Z";
			
			Date endDate=calendarUtil.getConvertedDate(format.parse(dateStr), 23, 59, 59);
			endTime=dateFormat.format(endDate)+"Z";
//		String startTime="2023-03-19T00:00:00.000Z";
//		String endTime="2023-03-25T23:59:59.000Z";
		
		JSONArray resultsArray = new JSONArray();
		
		String myurl = SAPServerConstants.TEMPORARY_TIME_INFO_BY_DATE_API.formatted(String.valueOf(top),
				String.valueOf(skip),startTime,endTime);
//		String myurl = SAPServerConstants.TEMPORARY_TIME_INFO_API.formatted(String.valueOf(top),
//				String.valueOf(skip));
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
	public HttpGet getSAPGetRequest(String myurl) throws Exception {
		HttpGet request = new HttpGet(myurl);
		request.setHeader(ApplicationConstants.HEADER_CONTENT_TYPE, ApplicationConstants.APPLICATION_JSON);
		String auth = username + ApplicationConstants.DELIMITER_COLON + password;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
		String authHeader = ApplicationConstants.BASIC_AUTH + new String(encodedAuth);
		request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		return request;

	}
	
	private LastSyncStatus setLastSyncStatus(Date currentDate) {
		LastSyncStatus lastSyncStatus = lastSyncStatusRepository.findByActivity("SF Temporary Time Info Sync");
		if(null!=lastSyncStatus)
			lastSyncStatus.setLastSyncTime(currentDate);
		else {
			    lastSyncStatus = new LastSyncStatus();
				lastSyncStatus.setActivity("SF Temporary Time Info Sync");
				lastSyncStatus.setLastSyncTime(currentDate);
		}
		return lastSyncStatus;
	}

}

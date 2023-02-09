package com.eikona.tech.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.constants.SAPServerConstants;
import com.eikona.tech.constants.ShiftConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Shift;
import com.eikona.tech.repository.ShiftRepository;
import com.eikona.tech.service.ShiftService;
import com.eikona.tech.util.GeneralSpecificationUtil;
import com.eikona.tech.util.RequestExecutionUtil;

@Service
@EnableScheduling
public class ShiftServiceImpl implements ShiftService {
	
	@Autowired
	private RequestExecutionUtil requestExecutionUtil;

	@Autowired
	private ShiftRepository shiftRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Shift> generalSpecificationShift;
	
	@Override
	public PaginationDto<Shift> searchByField(Long id, String name, String sDate, String day, int pageno,
			String sortField, String sortDir) {

		if (sDate.isEmpty() && day.isEmpty()) {
			SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
			try {
				sDate = format.format(new Date());
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
		
		Page<Shift> page = getShiftPage(id, name, sDate, day, pageno, sortField, sortDir);
		List<Shift> shiftList = page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC: ApplicationConstants.ASC;
		
		PaginationDto<Shift> dtoList = new PaginationDto<Shift>(shiftList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(),
				page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		
		return dtoList;
	}
	
	private Page<Shift> getShiftPage(Long id, String name, String date, String day, int pageno, String sortField,
			String sortDir) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<Shift> isDeletedFalse = generalSpecificationShift.isDeletedSpecification(false);
		Specification<Shift> idSpc = generalSpecificationShift.longSpecification(id, ApplicationConstants.ID);
		Specification<Shift> nameSpc = generalSpecificationShift.stringSpecification(name.toLowerCase(), ApplicationConstants.NAME);
		Specification<Shift> dateStrSpc = generalSpecificationShift.stringEqualSpecification(date, "dateStr");
		Specification<Shift> daySpc = generalSpecificationShift.stringEqualSpecification(day, "day");

		Page<Shift> page = shiftRepository.findAll(idSpc.and(nameSpc).and(dateStrSpc).and(daySpc).and(isDeletedFalse),pageable);
		return page;
	}

//	@Scheduled(fixedDelay = 30000)
//	@Scheduled(cron="0 0 23 * * *")
	public void syncShiftListFromSap() {
		JSONArray resultsArray = getShiftListFromSap();

		if (null != resultsArray && !resultsArray.isEmpty()) {
			List<Shift> shiftList = new ArrayList<Shift>();
			for (int i = NumberConstants.ZERO; i < resultsArray.size(); i++) {
				JSONObject currentObj = (JSONObject) resultsArray.get(i);
				
				List<Shift> shiftListObj = setShiftDetails(currentObj);
				
				shiftList.addAll(shiftListObj);
			}
			enrollShiftInMataFromSap(shiftList);
		}
	}

	private List<Shift> setShiftDetails(JSONObject jsonObj) {
		JSONObject workScheduleDayModel = (JSONObject) jsonObj.get(SAPServerConstants.WORK_SCHEDULE_DAY_MODELS);
		List<Shift> shiftList = new ArrayList<Shift>();
		if(null != workScheduleDayModel) {
			JSONArray results = (JSONArray) workScheduleDayModel.get(SAPServerConstants.RESULTS);
			
			Shift shift = new Shift();
			for (int i = NumberConstants.ZERO; i < results.size(); i++) {
				JSONObject currentObj = (JSONObject) results.get(i);
				
				shift = setShiftDayModel(currentObj);
				
				if(ShiftConstants.FLEXIBLE_GENERAL_PATTERN.equalsIgnoreCase(shift.getName()) || ShiftConstants.IND_GENSHIFT.equalsIgnoreCase(shift.getName())) {
					shift.setAllowAtAnyTime(true);
				}
				
				shiftList.add(shift);
				
			}
		}
		return shiftList;
	}

	private Shift setShiftDayModel(JSONObject currentObj) {
		SimpleDateFormat timeFormat = new SimpleDateFormat(ApplicationConstants.TIME_FORMAT_24HR);
		String day = (String)currentObj.get(SAPServerConstants.DAY);
		String workScheduleCode = (String)currentObj.get(SAPServerConstants.WORK_SCHEDULE_EXTERNAL_CODE);
		
		Shift shift = shiftRepository.findByDayAndName(day, workScheduleCode);
		 
		if(null == shift) {
			shift = new Shift();
		}else {
			shift.setCreatedBy(shift.getCreatedBy());
			shift.setCreatedDate(shift.getCreatedDate());
		}
		String dateStr = Year.of(LocalDate.now().getYear()).atDay( Integer.valueOf(day) ).toString();
		shift.setDateStr(dateStr);
		shift.setName(workScheduleCode);
		shift.setDay(day);
		
		if(!SAPServerConstants.OFF.equalsIgnoreCase((String)currentObj.get(SAPServerConstants.CATEGORY))) {
			
			JSONObject dayModelNav = (JSONObject) currentObj.get(SAPServerConstants.DAY_MODEL_NAV);
			JSONObject segments = (JSONObject) dayModelNav.get(SAPServerConstants.SEGMENTS);
			JSONArray segmentsResults = (JSONArray) segments.get(SAPServerConstants.RESULTS);
			for(int j = NumberConstants.ZERO; j < segmentsResults.size(); j++) {
				JSONObject segmentsResult = (JSONObject) segmentsResults.get(j);
				String category  = String.valueOf(segmentsResult.get(SAPServerConstants.CATEGORY));
				
				if(SAPServerConstants.SCHEDULED_WORKING_TIME.equalsIgnoreCase(category)) {
					Duration startDuration = Duration.parse((String) segmentsResult.get(SAPServerConstants.START_TIME));
					Duration endtDuration = Duration.parse((String) segmentsResult.get(SAPServerConstants.END_TIME));
					
					String startTimeInHms=String.format(SAPServerConstants.TIME_FORMAT_CONVERSION,  startDuration.toHours(), 
							startDuration.toMinutesPart(), startDuration.toSecondsPart());
					String endTimeInHms=String.format(SAPServerConstants.TIME_FORMAT_CONVERSION,  endtDuration.toHours(), 
							endtDuration.toMinutesPart(), endtDuration.toSecondsPart());
					
					try {
						shift.setStartTime(timeFormat.parse(startTimeInHms));
						shift.setEndTime(timeFormat.parse(endTimeInHms));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return shift;
	}

	private JSONArray getShiftListFromSap() {
		JSONArray resultsArray = new JSONArray();
		try {
			String myurl = SAPServerConstants.SHIFT_MASTER_DATA_API;
			
			String newurl = myurl.replaceAll(ApplicationConstants.DELIMITER_SPACE,
					ApplicationConstants.DELIMITER_FORMAT_SPACE);
			resultsArray = getShiftResultsArray(newurl);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultsArray;

	}
	
	private JSONArray getShiftResultsArray(String newurl) throws Exception, ParseException {
		JSONArray resultsArray = new JSONArray();
		HttpGet request = requestExecutionUtil.getSAPGetRequest(newurl);
		String responeData = requestExecutionUtil.executeHttpGetRequest(request);
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
		JSONObject firstObj = (JSONObject) jsonResponse.get(SAPServerConstants.D);
		resultsArray = (JSONArray) firstObj.get(SAPServerConstants.RESULTS);

		return resultsArray;
	}

	public void enrollShiftInMataFromSap(List<Shift> shiftList) {
		
		for (Shift shift : shiftList) {
			if("Flexi_General_Pattern".equalsIgnoreCase(shift.getName()))
				shift.setAllowAtAnyTime(true);
			else
				shift.setAllowAtAnyTime(false);
		}
		
		shiftRepository.saveAll(shiftList);
	}
}
package com.eikona.tech.service.impl;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.BioSecurityConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.repository.AccessLevelRepository;
import com.eikona.tech.service.AccessLevelService;
import com.eikona.tech.util.GeneralSpecificationUtil;
import com.eikona.tech.util.RequestExecutionUtil;

@Service
@EnableScheduling
public class AccessLevelServiceImpl implements AccessLevelService {
	
	@Autowired
	private AccessLevelRepository accessLevelRepository;
	
	@Autowired
	private RequestExecutionUtil requestExecutionUtil;
	
	@Value("${biosecurity.host.url}")
    private String host;
	
	@Value("${biosecurity.server.port}")
	private String port;
	
	@Value("${biosecurity.api.accesstoken}")
    private String accesstoken;
	
	@Autowired
	private GeneralSpecificationUtil<AccessLevel> generalSpecification;
	
//	@Scheduled(cron="0 0 4 * * *")
	public void syncAndSaveAccessLevel() {
		try {
			List<AccessLevel> accessLevelList =syncAccessLevel();
				accessLevelRepository.saveAll(accessLevelList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public List<AccessLevel> syncAccessLevel() {
		List<AccessLevel> accessLevelList = new ArrayList<AccessLevel>();
		try {
			String myurl=ApplicationConstants.HTTP_COLON_DOUBLE_SLASH+host+ ApplicationConstants.DELIMITER_COLON
					+ port +BioSecurityConstants.ACC_LEVEL_SYNC_API+accesstoken;
		    HttpGet request = new HttpGet(myurl);
		
		    String responeData =requestExecutionUtil.executeHttpGetRequest(request);
			
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONArray responseArray = (JSONArray) jsonResponse.get(BioSecurityConstants.DATA);
			
			setAccessLevelList(accessLevelList, responseArray);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return accessLevelList;
	}
	private void setAccessLevelList(List<AccessLevel> accessLevelList, JSONArray responseArray) {
		for(int i=NumberConstants.ZERO; i<responseArray.size(); i++) {
			JSONObject currentObj = (JSONObject) responseArray.get(i);
			String accName=(String)currentObj.get(ApplicationConstants.NAME);
			AccessLevel accessLevel = accessLevelRepository.findByName(accName.trim());
			if(null!=accessLevel) {
				accessLevel.setAccessId((String)currentObj.get(ApplicationConstants.ID));
			}else {
				accessLevel = new AccessLevel();
				
				accessLevel.setName(accName.trim());
				accessLevel.setAccessId((String)currentObj.get(ApplicationConstants.ID));
			}
			
				accessLevelList.add(accessLevel);
		}
	}

	@Override
	public PaginationDto<AccessLevel> searchByField(String name,String zone,String building,String plant, int pageno, String sortField, String sortDir) {
		
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}

		Page<AccessLevel> page = getPaginatedAccessLevel(name,zone,building,plant,pageno, sortField, sortDir);
        List<AccessLevel> accessLevelList =  page.getContent();
        
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<AccessLevel> dtoList = new PaginationDto<AccessLevel>(accessLevelList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}


	private Page<AccessLevel> getPaginatedAccessLevel(String name,String zone,String building,String plant, int pageno, String sortField,
			String sortDir) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending(): Sort.by(sortField).descending();
		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<AccessLevel> nameSpc = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<AccessLevel> zoneSpc = generalSpecification.foreignKeyStringSpecification(zone,"zone", ApplicationConstants.NAME);
		Specification<AccessLevel> buildingSpc = generalSpecification.foreignKeyStringSpecification(building,"building", ApplicationConstants.NAME);
		Specification<AccessLevel> plantSpc = generalSpecification.foreignKeyDoubleStringSpecification(plant,"building","plant", ApplicationConstants.NAME);
		
    	Page<AccessLevel> page = accessLevelRepository.findAll(nameSpc.and(plantSpc).and(buildingSpc).and(zoneSpc),pageable);
		return page;
	}
	
	@Override
	public List<AccessLevel> getAll() {

		return (List<AccessLevel>) accessLevelRepository.findAll();
	}
	
	@Override
	public AccessLevel save(AccessLevel accessLevel) {
		return accessLevelRepository.save(accessLevel);
	}
	
	@Override
	public AccessLevel getById(long id) {
		return accessLevelRepository.findById(id).get();
	}
	
	@Override
	public void deletedById(long id) {
		accessLevelRepository.deleteById(id);
	}
	
	@Override
	public List<AccessLevel> getByPlantAndBuilding(String plant, String building) {
		
		return accessLevelRepository.findByPlantAndByildingCustom(plant, building);
	}
}

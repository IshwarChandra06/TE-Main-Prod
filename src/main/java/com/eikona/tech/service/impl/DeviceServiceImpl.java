package com.eikona.tech.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.BioSecurityConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Device;
import com.eikona.tech.repository.DeviceRepository;
import com.eikona.tech.service.DeviceService;
import com.eikona.tech.util.GeneralSpecificationUtil;
import com.eikona.tech.util.RequestExecutionUtil;

@Service
@EnableScheduling
public class DeviceServiceImpl implements DeviceService {

	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Device> generalSpecification;
	
	@Autowired
	private RequestExecutionUtil requestExecutionUtil;
	
	@Value("${biosecurity.host.url}")
    private String host;
	
	@Value("${biosecurity.server.port}")
	private String port;
	
	@Value("${biosecurity.api.accesstoken}")
    private String accesstoken;
	
//	@Scheduled(cron = "0 15 2 * * *")
	public void syncAndSaveDoor() {
		try {
			List<Device> deviceList =syncDoor();
				deviceRepository.saveAll(deviceList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Device> syncDoor() {
		List<Device> deviceList = new ArrayList<Device>();
		try {
			String myurl=ApplicationConstants.HTTP_COLON_DOUBLE_SLASH+host+ ApplicationConstants.DELIMITER_COLON
					+ port +BioSecurityConstants.DOOR_SYNC_API+accesstoken;
		    HttpGet request = new HttpGet(myurl);
		
		    String responeData =requestExecutionUtil.executeHttpGetRequest(request);
			
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONArray responseArray = (JSONArray) jsonResponse.get(BioSecurityConstants.DATA);
			
			setDoorList(deviceList, responseArray);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return deviceList;
	}
	private void setDoorList(List<Device> deviceList, JSONArray responseArray) {
		for(int i=NumberConstants.ZERO; i<responseArray.size(); i++) {
			JSONObject currentObj = (JSONObject) responseArray.get(i);
			
			 Device device =deviceRepository.findByDoorId((String)currentObj.get(ApplicationConstants.ID));
			if(null!=device) {
				device.setName((String)currentObj.get(ApplicationConstants.NAME));
				device.setDeviceId((String)currentObj.get(ApplicationConstants.DEVICE_ID));
			}
			else {
				device = new Device();
				device.setDoorId((String)currentObj.get(ApplicationConstants.ID));
				device.setName((String)currentObj.get(ApplicationConstants.NAME));
				device.setDeviceId((String)currentObj.get(ApplicationConstants.DEVICE_ID));
			}
			
				deviceList.add(device);
		}
	}

	@Override
	public PaginationDto<Device> searchByField(String name,String zone,String building,String plant,String accesslevel,String serialNo,String ipAddress,String status, int pageno, String sortField, String sortDir) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Device> page = getDevicePage(name,zone,building,plant,accesslevel,serialNo,ipAddress,status, pageno, sortField, sortDir);
		List<Device> deviceList = page.getContent();

		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC : ApplicationConstants.ASC;
		PaginationDto<Device> dtoList = new PaginationDto<Device>(deviceList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir,
				ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Device> getDevicePage(String name,String zone,String building,String plant,String accesslevel,String serialNo, String ipAddress, String status, int pageno, String sortField,
			String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Device> isDeletedSpc = generalSpecification.booleanSpecification(false, "isDeleted");
		Specification<Device> nameSpc = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<Device> serialNoSpc = generalSpecification.stringSpecification(serialNo, "serialNo");
		Specification<Device> statusSpc = generalSpecification.stringSpecification(status, "status");
		Specification<Device> ipAddressSpec = generalSpecification.stringSpecification(ipAddress, "ipAddress");
		Specification<Device> zoneSpc = generalSpecification.foreignKeyDoubleStringSpecification(zone, "accessLevel","zone",ApplicationConstants.NAME);
		Specification<Device> acclevelSpc = generalSpecification.foreignKeyStringSpecification(accesslevel,"accessLevel", ApplicationConstants.NAME);
		Specification<Device> plantSpc = generalSpecification.foreignKeyTripleSpecification(plant,"accessLevel","building","plant", ApplicationConstants.NAME);
		Specification<Device> buildingSpc = generalSpecification.foreignKeyDoubleStringSpecification(building,"accessLevel","building", ApplicationConstants.NAME);

		Page<Device> page = deviceRepository.findAll(nameSpc.and(zoneSpc).and(acclevelSpc).and(plantSpc).and(buildingSpc).and(statusSpc).and(ipAddressSpec).and(serialNoSpc).and(isDeletedSpc), pageable);
		return page;
	}

	@Override
	public void deleteById(long id) {
		Optional<Device> optional = deviceRepository.findById(id);
		Device device = null;
		if (optional.isPresent()) {
			device = optional.get();
			device.setDeleted(true);
		} else {
			throw new RuntimeException("Device Not Found For Id" + id);
		}
		this.deviceRepository.save(device);

	}

	@Override
	public Device getById(long id) {
		return deviceRepository.findById(id).get();
	}

	@Override
	public void save(Device device) {
		 deviceRepository.save(device);
		
	}
}

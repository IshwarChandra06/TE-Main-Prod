package com.eikona.tech.service.impl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Device;
import com.eikona.tech.repository.AccessLevelRepository;
import com.eikona.tech.repository.DeviceRepository;
import com.eikona.tech.util.BioSecurityServerUtil;
import com.eikona.tech.util.EntityMap;

@Service
@EnableScheduling
public class AccessLevelDoorServiceImpl {
	
	@Autowired
	private AccessLevelRepository accLevelRepository; 

	@Autowired
	private BioSecurityServerUtil bioSecurityServerUtil;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private AccessLevelRepository accessLevelRepository;
	
	@Autowired
	private EntityMap entityMap;

//	@Scheduled(cron = "0 45 2 * * *")
	public void syncAccessLevelDoorFromBioSecurity() {
		try {
			String query = "select * from acc_level_device";

			ResultSet resultSet = bioSecurityServerUtil.jdbcConnection(query);

			List<AccessLevel> accLevelDoors = new ArrayList<AccessLevel>();
			Map<String, AccessLevel> accLevelMap = entityMap.getAccessLevelByAccessId();
			Map<String, Device> deviceMap = entityMap.getDoorByDoorId();
			
			if(null != resultSet) {
				while (resultSet.next()) {

					String acclevelId = resultSet.getString("level_id");
					String deviceId = resultSet.getString("device_id");
					
					
					AccessLevel accLevel= accLevelMap.get(acclevelId);
					Device device= deviceMap.get(deviceId);
					List<Device> deviceList = new ArrayList<Device>();
					
					if(null != accLevel) {
						setDoorsInAccessLevel(deviceId, accLevel, device, deviceList);
						
					}else {
						accLevel = new AccessLevel();
						accLevel.setAccessId(acclevelId);
						setDoorsInAccessLevel(deviceId, accLevel, device, deviceList);
						
					}
					accLevelDoors.add(accLevel);
						
				}
				accLevelRepository.saveAll(accLevelDoors);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setDoorsInAccessLevel(String deviceId, AccessLevel accLevel, Device device, List<Device> deviceList) {
		if(null!=device) {
			deviceList.add(device);
			accLevel.setDevice(deviceList);	
		}
		else {
			device = new Device();
			device.setDoorId(deviceId);
			device=deviceRepository.save(device);
			deviceList.add(device);
			accLevel.setDevice(deviceList);	
		}
	}
}

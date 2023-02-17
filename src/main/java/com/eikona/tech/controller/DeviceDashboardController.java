package com.eikona.tech.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.CountDto;
import com.eikona.tech.dto.DeviceDto;
import com.eikona.tech.dto.DeviceStatusDto;
import com.eikona.tech.dto.TransactionDto;
import com.eikona.tech.entity.Building;
import com.eikona.tech.entity.Device;
import com.eikona.tech.entity.Plant;
import com.eikona.tech.repository.BuildingRepository;
import com.eikona.tech.repository.DeviceRepository;
import com.eikona.tech.repository.PlantRepository;
import com.eikona.tech.repository.TransactionRepository;

@Controller
public class DeviceDashboardController {
	
	@Autowired
	private PlantRepository plantRepository;
	
	@Autowired
	private BuildingRepository buildingRepository;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@GetMapping({"/home","/"})
	public String dashboard(Model model) {
		try {
			CountDto countDto = new CountDto();
			List<Device> deviceList= deviceRepository.findAllByIsDeletedFalse();
			List<Plant> plantList= plantRepository.findAllByIsDeletedFalse();
			List<Building> buildingList= buildingRepository.findAllByIsDeletedFalse();
			
			List<Device> onlineDeviceList = new ArrayList<>();
			List<Device> offlineDeviceList = new ArrayList<>();
			for(Device device:deviceList) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dateStr = format.format(new Date());
				Date date = format.parse(dateStr);
				Date lastonline = device.getLastOnline();
				
				long mileseconds = date.getTime() - lastonline.getTime();
				
				if(mileseconds<=900000){
					onlineDeviceList.add(device);
				}else if(mileseconds>900000) {
					offlineDeviceList.add(device);
				}
			}
			long totalDeviceInstalled= deviceList.size();
			long totalOnlineDevice = onlineDeviceList.size();
			long totalOfflineDevice = offlineDeviceList.size();
			long totalPlant = plantList.size();
			long totalBuilding = buildingList.size();
			
			countDto.setTotalInstalledDevice(totalDeviceInstalled);
			countDto.setTotalOnline(totalOnlineDevice);
			countDto.setTotalOffline(totalOfflineDevice);
			countDto.setTotalPlant(totalPlant);
			countDto.setTotalBuilding(totalBuilding);
			
			model.addAttribute("countDto", countDto);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "dashboard";
	}
	
	@SuppressWarnings("unchecked")
	public @ResponseBody JSONArray deviceStatus() {
		JSONArray jsonArray = new JSONArray();
		try {
			List<Building> buildingList= buildingRepository.findAllByIsDeletedFalse();
			
			
			for(Building building:buildingList) {
				
				DeviceStatusDto deviceDto= new DeviceStatusDto();
				List<Device> deviceList=deviceRepository.findByBuildingAndIsDeletedFalseCustom(building.getName());
				List<Device> onlineDeviceList = new ArrayList<>();
					for(Device device:deviceList) {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String dateStr = format.format(new Date());
						Date date = format.parse(dateStr);
						Date lastonline = device.getLastOnline();
						
						long mileseconds = date.getTime() - lastonline.getTime();
						if(mileseconds<=900000){
							onlineDeviceList.add(device);
						}
					}
				deviceDto.setBuilding(building.getName());
				deviceDto.setOnlineDevice(onlineDeviceList.size());
				deviceDto.setTotalDevice(deviceList.size());
				jsonArray.add(deviceDto);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return jsonArray;
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value="/common-chart-data")
	@PreAuthorize("hasAuthority('device_dashboard_view')")
	public @ResponseBody JSONObject commonChart() {
		
		JSONObject returnObject = new JSONObject();
		
		JSONArray deviceArray = deviceStatus();
		returnObject.put("device", deviceArray);
		
//		JSONObject employeeObject = employeeLoginChart();
//		returnObject.put("employee", employeeObject);
		
		return returnObject;
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value="/employee-login-chart")
	@PreAuthorize("hasAuthority('device_dashboard_view')")
	public @ResponseBody JSONObject employeeLoginChart() {
		  JSONObject returnObject = new JSONObject();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		List<TransactionDto> listTransaction = transactionRepository.findTransactionByPunchDateStrCustom(format.format(new Date()));
		
		JSONArray employeeArray = new JSONArray();
		 for (TransactionDto companyDto : listTransaction) {
		    	JSONArray currObject = new JSONArray();
		    	currObject.add(companyDto.getPlant());
		    	currObject.add(companyDto.getLoginEmployee());
		    	employeeArray.add(currObject);
		    	
			}
		 returnObject.put("data",employeeArray);
		    return returnObject;
	}
	
	
	
	@GetMapping(value="/device-table")
	@PreAuthorize("hasAuthority('device_dashboard_view')")
	public @ResponseBody List<DeviceDto> deviceInfo() {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		List<Device> deviceList = deviceRepository.findAllByIsDeletedFalseOrderByPlantCustom();
		List<DeviceDto> deviceDtoList = new ArrayList<>();
		for (Device device : deviceList) {
			DeviceDto deviceDto = new DeviceDto();
			Long transactionCount = transactionRepository.findEventCountByDateAndDeviceCustom(format.format(new Date()),
					device.getName());
			Long unregisterTransaction = transactionRepository
					.findUnregisterCountByDateAndDeviceCustom(format.format(new Date()), device.getName());
			deviceDto.setCapacity(10000);
			deviceDto.setDevice(device.getName());
			deviceDto.setSerialNo(device.getSerialNo());
			if(null!=device.getAccessLevel()) {
				if(null!=device.getAccessLevel().getBuilding()) {
					deviceDto.setBuilding(device.getAccessLevel().getBuilding().getName());
					if(null!=device.getAccessLevel().getBuilding().getPlant()) 
					  deviceDto.setPlant(device.getAccessLevel().getBuilding().getPlant().getName());
				}
				
			}
			deviceDto.setTotalTransaction(transactionCount);
			deviceDto.setTotalUnregisterTransaction(unregisterTransaction);
			deviceDtoList.add(deviceDto);
		}

		return deviceDtoList;
	}
}

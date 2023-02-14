package com.eikona.tech.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.entity.DeviceHealthStatus;
import com.eikona.tech.export.ExportOfflineDevice;
import com.eikona.tech.repository.DeviceHealthStatusRepository;
import com.eikona.tech.util.CalendarUtil;

@Controller
public class DeviceHealthStatusController {
	
	@Autowired
	private DeviceHealthStatusRepository deviceHealthStatusRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Autowired
	private ExportOfflineDevice exportOfflineDevice;
	
	@GetMapping("/device-health-status/{id}")
	@PreAuthorize("hasAuthority('device_health_status')")
	public String deviceHealthStatus(@PathVariable(value = "id") long id, Model model) {
		
		model.addAttribute("id", id);
		return "device/device_health_status";
	}
	
	
	
	@GetMapping("/device-health-status-view")
	@PreAuthorize("hasAuthority('device_health_status')")
	public @ResponseBody List<DeviceHealthStatus> deviceHealthStatusView(Long id) {
		
		List<DeviceHealthStatus> deviceHealthStatusList = new ArrayList<>();
		try {
			
			Date startDate = calendarUtil.getDateByAddingHour(new Date(),-24);
			
			deviceHealthStatusList = deviceHealthStatusRepository.findByDeviceIdAndDateCustom(id, startDate, new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		for(int i=0; i<24; i++) {
//			DeviceHealthStatus deviceHealthStatus1 = new DeviceHealthStatus();
//			deviceHealthStatus1.setTimeStr("2023-02-13 "+i+":00:00");
//			deviceHealthStatus1.setStatus(0);
//			DeviceHealthStatus deviceHealthStatus2 = new DeviceHealthStatus();
//			deviceHealthStatus2.setStatus(1);
//			deviceHealthStatus2.setTimeStr("2023-02-13 "+i+":30:00");
//			
//			deviceHealthStatusList.add(deviceHealthStatus1);
//			deviceHealthStatusList.add(deviceHealthStatus2);
//			
//		}
		
		return deviceHealthStatusList;
	}
	
	@RequestMapping(value="/device-offline/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('device_export')")
	public void exportToFile(HttpServletResponse response,Long id,String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Device_Offline_" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportOfflineDevice.fileExportBySearchValue(response,id,flag );
			System.out.println();
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}

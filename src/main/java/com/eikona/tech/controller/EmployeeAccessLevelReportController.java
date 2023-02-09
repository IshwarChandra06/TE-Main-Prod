package com.eikona.tech.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Building;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.service.AccessLevelService;
import com.eikona.tech.service.BuildingService;
import com.eikona.tech.service.PlantService;
import com.eikona.tech.service.ZoneService;
import com.eikona.tech.service.impl.EmployeeAccessLevelReportServiceImpl;

@Controller
public class EmployeeAccessLevelReportController {
	
	@Autowired
	private PlantService plantService;
	
	@Autowired
	private BuildingService buildingService;
	
	@Autowired
	private AccessLevelService accessLevelService;
	
	@Autowired
	private ZoneService zoneService;
	
	@Autowired
	private EmployeeAccessLevelReportServiceImpl employeeAccessLevelReportServiceImpl;
	
	@GetMapping(value = "/employee/access-level/report")
	@PreAuthorize("hasAuthority('employee_access_level_report_view')")
	public String testReportPage(Model model) {
		
		model.addAttribute("listPlant", plantService.getAll());
		model.addAttribute("listZone", zoneService.getAll());
		return "reports/employee_access_level_report";
	}
	
	@GetMapping(value = "/get/building-name")
	@PreAuthorize("hasAuthority('employee_access_level_report_view')")
	public @ResponseBody List<Building> getBuilding(String[] plants) {
		
		List<Building> buildingList = buildingService.getByPlant(plants);
		
		return buildingList;
	}
	@GetMapping(value = "/get/access-level-name")
	@PreAuthorize("hasAuthority('employee_access_level_report_view')")
	public @ResponseBody List<AccessLevel> getAccessLevelName(String plant, String building) {
		
		List<AccessLevel> accessLevelList = accessLevelService.getByPlantAndBuilding(plant, building);
		
		return accessLevelList;
	}
	
	@RequestMapping(value = "/employee/access-level/report/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_access_level_report_view')")
	public @ResponseBody PaginationDto<Employee> searchAccessLevel(String plant, String building, String[] accessLevels,String zone, int pageno, String sortField, String sortDir) {
		
		PaginationDto<Employee> dtoList = employeeAccessLevelReportServiceImpl.searchByField(plant, building,  accessLevels,zone, pageno, sortField, sortDir);
		return dtoList;
	}
	
	@GetMapping(value = "/employee-access-level/export-to-file")
	@PreAuthorize("hasAuthority('employee_access_level_report_export')")
	public void exportDamageReport(HttpServletResponse response, String plant, String building, String[] accessLevels,String zone, String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			
			employeeAccessLevelReportServiceImpl.fileExportBySearchValue(response, plant, building, accessLevels,zone, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}

}

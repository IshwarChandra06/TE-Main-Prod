package com.eikona.tech.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Device;
import com.eikona.tech.export.ExportAccessLevel;
import com.eikona.tech.repository.DeviceRepository;
import com.eikona.tech.service.AccessLevelService;
import com.eikona.tech.service.BuildingService;
import com.eikona.tech.service.ZoneService;
import com.eikona.tech.service.impl.AccessLevelDoorServiceImpl;
import com.eikona.tech.service.impl.AccessLevelServiceImpl;

@Controller
public class AccessLevelController {
	
	@Autowired
	private AccessLevelService accessLevelService;
	
	@Autowired
	private AccessLevelServiceImpl accessLevelServiceImpl;

	@Autowired
	private AccessLevelDoorServiceImpl accessLevelDoorServiceImpl;
	
	@Autowired
	private BuildingService buildingService;
	
	@Autowired
	private ZoneService zoneService;

	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private ExportAccessLevel exportAccessLevel;
	
	
	@GetMapping("/access-level")
	@PreAuthorize("hasAuthority('access_level_view')")
	public String list() {
		return "accessLevel/access_level_list";
		
	}
	
	
	@GetMapping("/access-level/new")
	@PreAuthorize("hasAuthority('access_level_create')")
	public String newArea(Model model) {
		List<Device> deviceList = (List<Device>) deviceRepository.findAll(); 
		model.addAttribute("listZone", zoneService.getAll());
		model.addAttribute("listBuilding", buildingService.getAll());
		model.addAttribute("accessLevel", new AccessLevel());
		model.addAttribute("listDevice", deviceList);
		model.addAttribute("title", "New Access Level");
		return "accessLevel/access_level_new";
	}
	
	@PostMapping("/access-level/add")
	@PreAuthorize("hasAnyAuthority('access_level_create','access_level_update')")
	public String saveArea(@ModelAttribute("accessLevel") AccessLevel accessLevel, @Valid AccessLevel ar, Errors errors, String title,
			Model model) {
		if (errors.hasErrors()) {
			List<Device> deviceList = (List<Device>) deviceRepository.findAll(); 
			model.addAttribute("listBuilding", buildingService.getAll());
			model.addAttribute("listZone", zoneService.getAll());
			model.addAttribute("accessLevel", accessLevel);
			model.addAttribute("listDevice", deviceList);
			model.addAttribute("title", title);
			return "accessLevel/access_level_new";
		} else {
			if (null == accessLevel.getId())
				accessLevelService.save(accessLevel);
			else {
				AccessLevel accessLevelObj = accessLevelService.getById(accessLevel.getId());
				accessLevel.setCreatedBy(accessLevelObj.getCreatedBy());
				accessLevel.setCreatedDate(accessLevelObj.getCreatedDate());
				accessLevelService.save(accessLevel);
			}
			return "redirect:/access-level";
		}

	}
	
	@GetMapping("/access-level/edit/{id}")
	@PreAuthorize("hasAuthority('access_level_update')")
	public String updateAccessLevel(@PathVariable(value = "id") long id, Model model) {
		
		List<Device> deviceList = (List<Device>) deviceRepository.findAll(); 
		AccessLevel accessLevel = accessLevelService.getById(id);
		model.addAttribute("listBuilding", buildingService.getAll());
		model.addAttribute("listDevice", deviceList);
		model.addAttribute("listZone", zoneService.getAll());
		model.addAttribute("accessLevel", accessLevel);
		model.addAttribute("title", "Update Access Level");
		return "accessLevel/access_level_new";
	}

	@GetMapping("/access-level/delete/{id}")
	@PreAuthorize("hasAuthority('access_level_delete')")
	public String deleteArea(@PathVariable(value = "id") long id) {

		this.accessLevelService.deletedById(id);
		return "redirect:/access-level";
	}

	@RequestMapping(value = "/access-level/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('access_level_view')")
	public @ResponseBody PaginationDto<AccessLevel> searchAccessLevel(String name,String zone,String building,String plant, int pageno, String sortField, String sortDir) {
		
		PaginationDto<AccessLevel> dtoList = accessLevelService.searchByField(name,zone,building,plant,pageno, sortField, sortDir);
		return dtoList;
	}
	
	@GetMapping("/sync/access-level")
	@PreAuthorize("hasAuthority('access_level_view')")
	public @ResponseBody void syncAccessLevel() {
		try {
			 accessLevelServiceImpl.syncAndSaveAccessLevel();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping("/sync/access-level-door")
	@PreAuthorize("hasAuthority('access_level_view')")
	public @ResponseBody void syncAccessLevelDoor() {
		try {
			accessLevelDoorServiceImpl.syncAccessLevelDoorFromBioSecurity();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value="/access-level/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('access_level_export')")
	public void exportToFile(HttpServletResponse response,String name,String zone,String building,String plant,String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Access_level_" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportAccessLevel.fileExportBySearchValue(response,name,zone,building,plant,flag );
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}

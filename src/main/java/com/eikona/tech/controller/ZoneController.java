package com.eikona.tech.controller;

import java.util.List;

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
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Zone;
import com.eikona.tech.repository.AccessLevelRepository;
import com.eikona.tech.service.AccessLevelService;
import com.eikona.tech.service.ZoneService;

@Controller
public class ZoneController {
	
	
	@Autowired
	private ZoneService zoneService;
	
	@Autowired
	private AccessLevelRepository accessLevelRepository;


	@GetMapping("/zone")
	@PreAuthorize("hasAuthority('zone_view')")
	public String zoneList() {
		return "zone/zone_list";
	}

	@GetMapping("/zone/new")
	@PreAuthorize("hasAuthority('zone_create')")
	public String newZone(Model model) {
		Zone zone = new Zone();
		model.addAttribute("zone", zone);
		model.addAttribute("title", "New Zone");
		return "zone/zone_new";
	}

	@PostMapping("/zone/add")
	@PreAuthorize("hasAnyAuthority('zone_create','zone_update')")
	public String saveZone(@ModelAttribute("zone") Zone zone, @Valid Zone desig,
			Errors errors,String title, Model model) {
		if (errors.hasErrors()) {
			model.addAttribute("title", title);
			return "zone/zone_new";
		} else {
			if(null==zone.getId())
				zoneService.save(zone);
 			else {
 				Zone zoneObj = zoneService.getById(zone.getId());
 				zone.setCreatedBy(zoneObj.getCreatedBy());
 				zone.setCreatedDate(zoneObj.getCreatedDate());
 				zoneService.save(zone);
 			}
			return "redirect:/zone";

		}

	}

	@GetMapping("/zone/edit/{id}")
	@PreAuthorize("hasAuthority('zone_update')")
	public String updateZone(@PathVariable(value = "id") long id, Model model) {
		Zone zone = zoneService.getById(id);
		model.addAttribute("zone", zone);
		model.addAttribute("title", "Update Zone");
		return "zone/zone_new";
	}

	@GetMapping("/zone/delete/{id}")
	@PreAuthorize("hasAuthority('zone_delete')")
	public String deleteZone(@PathVariable(value = "id") long id) {

		this.zoneService.deleteById(id);
		return "redirect:/zone";
	}
	@GetMapping("/zone-accesslevel/{id}")
	@PreAuthorize("hasAuthority('employee_view')")
	public String employeeAccessLevelAssociation(@PathVariable(value = "id") long id, Model model) {
		List<AccessLevel> accessLevelList = accessLevelRepository.findByZoneCustom(id);
		Zone zone = zoneService.getById(id);
		model.addAttribute("listAccesslevel", accessLevelList);
		model.addAttribute("zone", zone.getName());
		return "zone/zone_accesslevel";
	}
	@RequestMapping(value = "/search/zone", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('zone_view')")
	public @ResponseBody PaginationDto<Zone> searchAccessLevel(Long id, String name, int pageno, String sortField, String sortDir) {
		
		PaginationDto<Zone> dtoList = zoneService.searchByField(id, name,  pageno, sortField, sortDir);
		return dtoList;
	}

}

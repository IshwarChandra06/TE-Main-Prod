package com.eikona.tech.controller;

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
import com.eikona.tech.entity.Building;
import com.eikona.tech.service.BuildingService;
import com.eikona.tech.service.PlantService;

@Controller
public class BuildingController {
	
	@Autowired
	private BuildingService buildingService;
	
	@Autowired
	private PlantService plantService;
	
	@GetMapping(value={"/building"})
	@PreAuthorize("hasAuthority('building_view')")
	public String employeeList(Model model) {
		return "building/building_list";
	}
	
	@GetMapping("/building/new")
	@PreAuthorize("hasAuthority('building_create')")
	public String newArea(Model model) {
		
		model.addAttribute("listPlant", plantService.getAll());
		model.addAttribute("building", new Building());
		model.addAttribute("title", "New Building");
		return "building/building_new";
	}
	
	@PostMapping("/building/add")
	@PreAuthorize("hasAnyAuthority('building_create','building_update')")
	public String saveArea(@ModelAttribute("building") Building building, @Valid Building ar, Errors errors, String title,
			Model model) {
		if (errors.hasErrors()) {
			model.addAttribute("listPlant", plantService.getAll());
			model.addAttribute("building", building);
			model.addAttribute("title", title);
			return "building/building_new";
		} else {
			if (null == building.getId())
				buildingService.save(building);
			else {
				Building buildingObj = buildingService.getById(building.getId());
				building.setCreatedBy(buildingObj.getCreatedBy());
				building.setCreatedDate(buildingObj.getCreatedDate());
				buildingService.save(building);
			}
			return "redirect:/building";
		}

	}
	
	@GetMapping("/building/edit/{id}")
	@PreAuthorize("hasAuthority('building_update')")
	public String updateArea(@PathVariable(value = "id") long id, Model model) {
		Building building = buildingService.getById(id);
		model.addAttribute("listPlant", plantService.getAll());
		model.addAttribute("building", building);
		model.addAttribute("title", "Update Building");
		return "building/building_new";
	}

	@GetMapping("/building/delete/{id}")
	@PreAuthorize("hasAuthority('building_delete')")
	public String deleteArea(@PathVariable(value = "id") long id) {

		this.buildingService.deletedById(id);
		return "redirect:/building";
	}

	@RequestMapping(value = "/search/building", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('building_view')")
	public @ResponseBody PaginationDto<Building> search(Long id, String name, String plant, int pageno, String sortField,
			String sortDir) {

		PaginationDto<Building> dtoList = buildingService.searchByField(id, name, plant, pageno, sortField, sortDir);
		return dtoList;
	}

}

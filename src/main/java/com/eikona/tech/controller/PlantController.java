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
import com.eikona.tech.entity.Plant;
import com.eikona.tech.service.PlantService;

@Controller
public class PlantController {
	
	@Autowired
	private PlantService plantService;
	
	@GetMapping(value={"/plant"})
	@PreAuthorize("hasAuthority('plant_view')")
	public String employeeList(Model model) {
		return "plant/plant_list";
	}
	
	@GetMapping("/plant/new")
	@PreAuthorize("hasAuthority('plant_create')")
	public String newArea(Model model) {
		model.addAttribute("plant", new Plant());
		model.addAttribute("title", "New Plant");
		return "plant/plant_new";
	}
	
	@PostMapping("/plant/add")
	@PreAuthorize("hasAnyAuthority('plant_create','plant_update')")
	public String saveArea(@ModelAttribute("plant") Plant plant, @Valid Plant ar, Errors errors, String title,
			Model model) {
		if (errors.hasErrors()) {
			model.addAttribute("plant", plant);
			model.addAttribute("title", title);
			return "plant/plant_new";
		} else {
			if (null == plant.getId())
				plantService.save(plant);
			else {
				Plant plantObj = plantService.getById(plant.getId());
				plant.setCreatedBy(plantObj.getCreatedBy());
				plant.setCreatedDate(plantObj.getCreatedDate());
				plantService.save(plant);
			}
			return "redirect:/plant";
		}

	}
	
	@GetMapping("/plant/edit/{id}")
	@PreAuthorize("hasAuthority('plant_update')")
	public String updateArea(@PathVariable(value = "id") long id, Model model) {
		Plant plant = plantService.getById(id);
		model.addAttribute("plant", plant);
		model.addAttribute("title", "Update Plant");
		return "plant/plant_new";
	}

	@GetMapping("/plant/delete/{id}")
	@PreAuthorize("hasAuthority('plant_delete')")
	public String deleteArea(@PathVariable(value = "id") long id) {

		this.plantService.deletedById(id);
		return "redirect:/plant";
	}

	@RequestMapping(value = "/search/plant", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('plant_view')")
	public @ResponseBody PaginationDto<Plant> search(Long id, String name, String city,  String pinCode, int pageno, String sortField,
			String sortDir) {

		PaginationDto<Plant> dtoList = plantService.searchByField(id, name, city, pinCode, pageno, sortField, sortDir);
		return dtoList;
	}

}

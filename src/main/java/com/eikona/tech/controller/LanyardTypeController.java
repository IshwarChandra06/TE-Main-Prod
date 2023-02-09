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
import com.eikona.tech.entity.LanyardType;
import com.eikona.tech.service.LanyardTypeService;

@Controller
public class LanyardTypeController {
	
	
	@Autowired
	private LanyardTypeService lanyardTypeService;


	@GetMapping("/lanyard-type")
	@PreAuthorize("hasAuthority('lanyard_type_view')")
	public String lanyardTypeList() {
		return "lanyardType/lanyard_type_list";
	}

	@GetMapping("/lanyard-type/new")
	@PreAuthorize("hasAuthority('lanyard_type_create')")
	public String newLanyardType(Model model) {
		LanyardType lanyardType = new LanyardType();
		model.addAttribute("lanyardType", lanyardType);
		model.addAttribute("title", "New Lanyard Type");
		return "lanyardType/lanyard_type_new";
	}

	@PostMapping("/lanyard-type/add")
	@PreAuthorize("hasAnyAuthority('lanyard_type_create','lanyard_type_update')")
	public String saveLanyardType(@ModelAttribute("lanyardType") LanyardType lanyardType, @Valid LanyardType desig,
			Errors errors,String title, Model model) {
		if (errors.hasErrors()) {
			model.addAttribute("title", title);
			return "lanyardType/lanyard_type_new";
		} else {
			if(null==lanyardType.getId())
				lanyardTypeService.save(lanyardType);
 			else {
 				LanyardType lanyardTypeObj = lanyardTypeService.getById(lanyardType.getId());
 				lanyardType.setCreatedBy(lanyardTypeObj.getCreatedBy());
 				lanyardType.setCreatedDate(lanyardTypeObj.getCreatedDate());
 				lanyardTypeService.save(lanyardType);
 			}
			return "redirect:/lanyard-type";

		}

	}

	@GetMapping("/lanyard-type/edit/{id}")
	@PreAuthorize("hasAuthority('lanyard_type_update')")
	public String updateLanyardType(@PathVariable(value = "id") long id, Model model) {
		LanyardType lanyardType = lanyardTypeService.getById(id);
		model.addAttribute("lanyardType", lanyardType);
		model.addAttribute("title", "Update Lanyard Type");
		return "lanyardType/lanyard_type_new";
	}

	@GetMapping("/lanyard-type/delete/{id}")
	@PreAuthorize("hasAuthority('lanyard_type_delete')")
	public String deleteLanyardType(@PathVariable(value = "id") long id) {

		this.lanyardTypeService.deleteById(id);
		return "redirect:/lanyard-type";
	}

	@RequestMapping(value = "/search/lanyard-type", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('lanyard_type_view')")
	public @ResponseBody PaginationDto<LanyardType> searchAccessLevel(Long id, String name, int pageno, String sortField, String sortDir) {
		
		PaginationDto<LanyardType> dtoList = lanyardTypeService.searchByField(id, name,  pageno, sortField, sortDir);
		return dtoList;
	}

}

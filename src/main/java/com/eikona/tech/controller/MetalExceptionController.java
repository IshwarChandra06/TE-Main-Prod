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
import com.eikona.tech.entity.MetalException;
import com.eikona.tech.service.MetalExceptionService;

@Controller
public class MetalExceptionController {
	
	
	@Autowired
	private MetalExceptionService metalExceptionService;


	@GetMapping("/metal-exception")
	@PreAuthorize("hasAuthority('metal_exception_view')")
	public String metalExceptionList() {
		return "metalException/metal_exception_list";
	}

	@GetMapping("/metal-exception/new")
	@PreAuthorize("hasAuthority('metal_exception_create')")
	public String newMetalException(Model model) {
		MetalException metalException = new MetalException();
		model.addAttribute("metalException", metalException);
		model.addAttribute("title", "New Metal Exception");
		return "metalException/metal_exception_new";
	}

	@PostMapping("/metal-exception/add")
	@PreAuthorize("hasAnyAuthority('metal_exception_create','metal_exception_update')")
	public String saveMetalException(@ModelAttribute("metalException") MetalException metalException, @Valid MetalException desig,
			Errors errors,String title, Model model) {
		if (errors.hasErrors()) {
			model.addAttribute("title", title);
			return "metalException/metal_exception_new";
		} else {
			if(null==metalException.getId())
				metalExceptionService.save(metalException);
 			else {
 				MetalException metalExceptionObj = metalExceptionService.getById(metalException.getId());
 				metalException.setCreatedBy(metalExceptionObj.getCreatedBy());
 				metalException.setCreatedDate(metalExceptionObj.getCreatedDate());
 				metalExceptionService.save(metalException);
 			}
			return "redirect:/metal-exception";
		}

	}

	@GetMapping("/metal-exception/edit/{id}")
	@PreAuthorize("hasAuthority('metal_exception_update')")
	public String updateMetalException(@PathVariable(value = "id") long id, Model model) {
		MetalException metalException = metalExceptionService.getById(id);
		model.addAttribute("metalException", metalException);
		model.addAttribute("title", "Update Metal Exception");
		return "metalException/metal_exception_new";
	}

	@GetMapping("/metal-exception/delete/{id}")
	@PreAuthorize("hasAuthority('metal_exception_delete')")
	public String deleteMetalException(@PathVariable(value = "id") long id) {

		this.metalExceptionService.deleteById(id);
		return "redirect:/metal-exception";
	}

	@RequestMapping(value = "/search/metal-exception", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('metal_exception_view')")
	public @ResponseBody PaginationDto<MetalException> searchAccessLevel(Long id, String name, int pageno, String sortField, String sortDir) {
		
		PaginationDto<MetalException> dtoList = metalExceptionService.searchByField(id, name,  pageno, sortField, sortDir);
		return dtoList;
	}

}

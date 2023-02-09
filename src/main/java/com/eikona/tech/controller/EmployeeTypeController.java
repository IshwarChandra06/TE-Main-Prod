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
import com.eikona.tech.entity.EmployeeType;
import com.eikona.tech.service.EmployeeTypeService;

@Controller
public class EmployeeTypeController {
	
	
	@Autowired
	private EmployeeTypeService employeeTypeService;


	@GetMapping("/employee-type")
	@PreAuthorize("hasAuthority('employee_type_view')")
	public String employeeTypeList() {
		return "employeeType/employee_type_list";
	}

	@GetMapping("/employee-type/new")
	@PreAuthorize("hasAuthority('employee_type_create')")
	public String newEmployeeType(Model model) {
		EmployeeType employeeType = new EmployeeType();
		model.addAttribute("employeeType", employeeType);
		model.addAttribute("title", "New EmployeeType");
		return "employeeType/employee_type_new";
	}

	@PostMapping("/employee-type/add")
	@PreAuthorize("hasAnyAuthority('employee_type_create','employee_type_update')")
	public String saveEmployeeType(@ModelAttribute("employeeType") EmployeeType employeeType, @Valid EmployeeType desig,
			Errors errors,String title, Model model) {
		if (errors.hasErrors()) {
			model.addAttribute("title", title);
			return "employeeType/employee_type_new";
		} else {
			if(null==employeeType.getId())
				employeeTypeService.save(employeeType);
 			else {
 				EmployeeType employeeTypeObj = employeeTypeService.getById(employeeType.getId());
 				employeeType.setCreatedBy(employeeTypeObj.getCreatedBy());
 				employeeType.setCreatedDate(employeeTypeObj.getCreatedDate());
 				employeeTypeService.save(employeeType);
 			}
			return "redirect:/employee-type";

		}

	}

	@GetMapping("/employee-type/edit/{id}")
	@PreAuthorize("hasAuthority('employee_type_update')")
	public String updateEmployeeType(@PathVariable(value = "id") long id, Model model) {
		EmployeeType employeeType = employeeTypeService.getById(id);
		model.addAttribute("employeeType", employeeType);
		model.addAttribute("title", "Update EmployeeType");
		return "employeeType/employee_type_new";
	}

	@GetMapping("/employee-type/delete/{id}")
	@PreAuthorize("hasAuthority('employee_type_delete')")
	public String deleteEmployeeType(@PathVariable(value = "id") long id) {

		this.employeeTypeService.deleteById(id);
		return "redirect:/employee-type";
	}

	@RequestMapping(value = "/search/employee-type", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_type_view')")
	public @ResponseBody PaginationDto<EmployeeType> searchAccessLevel(Long id, String name, int pageno, String sortField, String sortDir) {
		
		PaginationDto<EmployeeType> dtoList = employeeTypeService.searchByField(id, name,  pageno, sortField, sortDir);
		return dtoList;
	}

}

package com.eikona.tech.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.repository.EmployeeTypeRepository;
import com.eikona.tech.service.impl.ActiveEmployeeServiceImpl;

@Controller
public class ActiveEmployeeController {
	
	
	@Autowired
	private ActiveEmployeeServiceImpl activeemployeeServiceImpl;
	
	@Autowired
	private EmployeeTypeRepository employeeTypeRepository;
	
	@GetMapping(value = "/active-employee")
	@PreAuthorize("hasAuthority('active_employee_report_view')")
	public String reportPage(Model model) {
		model.addAttribute("listEmployeeType", employeeTypeRepository.findAllNameCustom());
		return "reports/active_employee_list";
	}
	
	@GetMapping(value = "/active-employee/search")
	@PreAuthorize("hasAuthority('active_employee_report_view')")
	public @ResponseBody PaginationDto<Employee> searchEmployee(String firstName, String lastName,String empId,String department,String designation,
			String employeeType,String cardNo, int pageno, String sortField, String sortDir) {
		PaginationDto<Employee> dtoList = activeemployeeServiceImpl.searchByField(firstName, lastName, empId, department,  designation, employeeType.trim(),cardNo, pageno, sortField, sortDir);
		return dtoList;
	}
	
	
	@GetMapping(value = "/active-employee/export-to-file")
	@PreAuthorize("hasAuthority('active_employee_report_export')")
	public void exportReport(HttpServletResponse response,String firstName, String lastName,String empId,String department,String designation,
			String employeeType,String cardNo, String flag) {
		

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			activeemployeeServiceImpl.fileExportBySearchValue(response, firstName, lastName, empId, department,  designation, employeeType.trim(),cardNo, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}

}

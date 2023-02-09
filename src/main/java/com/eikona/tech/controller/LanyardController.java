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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Lanyard;
import com.eikona.tech.export.ExportEmployeeLanyardManagement;
import com.eikona.tech.export.ExportLanyardManagement;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.service.EmployeeService;
import com.eikona.tech.service.LanyardService;
import com.eikona.tech.service.LanyardTypeService;

@Controller
public class LanyardController {
	
	@Autowired
	private LanyardService lanyardService;
	
	@Autowired
	private LanyardTypeService lanyardTypeService;
	
	@Autowired
	private EmployeeService employeeService;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ExportLanyardManagement exportLanyardUtil;
	
	@Autowired
	private ExportEmployeeLanyardManagement exportEmployeeLanyardManagement;
	
	@GetMapping(value={"/lanyard"})
	@PreAuthorize("hasAuthority('lanyard_view')")
	public String employeeList(Model model) {
		return "lanyard/lanyard_list";
	}
	
	@GetMapping("/lanyard/new")
	@PreAuthorize("hasAuthority('lanyard_create')")
	public String newLanyard(Model model) {
		
		model.addAttribute("listEmployee", employeeService.getAll());
		model.addAttribute("listLanyardType",lanyardTypeService.getAll());
		model.addAttribute("lanyard", new Lanyard());
		
		model.addAttribute("title", "New Lanyard Management");
		return "lanyard/lanyard_new";
	}
	
	@GetMapping("/employee-lanyard-view/{empId}")
	@PreAuthorize("hasAuthority('lanyard_view')")
	public String addEmployeeLanyardView(@PathVariable(value = "empId") String empId, Model model) {
		model.addAttribute("title", "Lanyard Management");
		model.addAttribute("empId", empId);
		model.addAttribute("redirect", "/employee/view/"+empId);
		return "lanyard/lanyard_list_view";
	}
	
	@GetMapping("/employee-lanyard/{empId}")
	@PreAuthorize("hasAuthority('lanyard_create')")
	public String addEmployeeLanyard(@PathVariable(value = "empId") String empId, Model model) {
		String firstName="";
		String lastName="";
		Employee employeeObj = employeeRepository.findByEmployeeId(empId);
		if(null!=employeeObj.getFirstName())
			firstName=employeeObj.getFirstName();
		if(null!=employeeObj.getLastName())
			lastName=employeeObj.getLastName();
		model.addAttribute("lanyard", new Lanyard());
	    model.addAttribute("employee", employeeObj);
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", employeeObj.getEmployeeId());
		model.addAttribute("listLanyardType",lanyardTypeService.getAll());
		model.addAttribute("title", "New Lanyard Management");
		model.addAttribute("redirect", "/employee-lanyard-view/");

		return "lanyard/lanyard_new";
	}
	
	@PostMapping("/lanyard/add")
	@PreAuthorize("hasAnyAuthority('lanyard_create','lanyard_update')")
	public String saveArea(@ModelAttribute("lanyard") Lanyard lanyard,String redirectUrl) {
		
		lanyardService.save(lanyard);
		if("/employee-lanyard-view/".equalsIgnoreCase(redirectUrl))
		    return "redirect:"+redirectUrl+lanyard.getEmployee().getEmployeeId();
		else
		    return "redirect:"+redirectUrl;
		}

	
	@GetMapping("/lanyard/edit/{id}")
	@PreAuthorize("hasAuthority('lanyard_update')")
	public String updateLanyard(@PathVariable(value = "id") long id, Model model) {
		Lanyard lanyard = lanyardService.getById(id);
		String firstName="";
		String lastName="";
		Employee employeeObj = lanyard.getEmployee();
		if(null!=employeeObj.getFirstName())
			firstName=employeeObj.getFirstName();
		if(null!=employeeObj.getLastName())
			lastName=employeeObj.getLastName();
		model.addAttribute("lanyard", new Lanyard());
	    model.addAttribute("employee", employeeObj);
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", employeeObj.getEmployeeId());
		model.addAttribute("listLanyardType",lanyardTypeService.getAll());
		model.addAttribute("lanyard", lanyard);
		model.addAttribute("title", "Update Lanyard Management");
		model.addAttribute("redirect", "/lanyard");
		return "lanyard/lanyard_new";
	}
	
	@GetMapping("/lanyard-view/edit/{id}")
	@PreAuthorize("hasAuthority('lanyard_update')")
	public String editLanyard(@PathVariable(value = "id") long id, Model model) {
		Lanyard lanyard = lanyardService.getById(id);
		String firstName="";
		String lastName="";
		Employee employeeObj = lanyard.getEmployee();
		if(null!=employeeObj.getFirstName())
			firstName=employeeObj.getFirstName();
		if(null!=employeeObj.getLastName())
			lastName=employeeObj.getLastName();
		model.addAttribute("lanyard", new Lanyard());
	    model.addAttribute("employee", employeeObj);
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", employeeObj.getEmployeeId());
		model.addAttribute("listLanyardType",lanyardTypeService.getAll());
		model.addAttribute("lanyard", lanyard);
		model.addAttribute("title", "Update Lanyard Management");
		model.addAttribute("redirect", "/employee-lanyard-view/");
		return "lanyard/lanyard_edit";
	}


	@RequestMapping(value = "/search/lanyard", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('lanyard_view')")
	public @ResponseBody PaginationDto<Lanyard> search(String sDate,String eDate,String employeeId,  String type, String status, int pageno, String sortField,
			String sortDir) {

		PaginationDto<Lanyard> dtoList = lanyardService.searchByField(sDate,eDate,employeeId, type, status, pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value = "/search/lanyard-view", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('lanyard_view')")
	public @ResponseBody PaginationDto<Lanyard> searchView(Long empId, int pageno, String sortField, String sortDir) {
		
		PaginationDto<Lanyard> dtoList = lanyardService.searchByFieldView(String.valueOf(empId),pageno, sortField, sortDir);
		return dtoList;
	}
	@GetMapping(value = "/lanyard/export-to-file")
	@PreAuthorize("hasAuthority('lanyard_export')")
	public void exportLanyardTrackReport(HttpServletResponse response, String sDate,String eDate,String employeeId,  String type, String status, String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportLanyardUtil.fileExportBySearchValue(response,sDate,eDate,employeeId, type, status, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping(value = "/employee-lanyard-management/export-to-file")
	@PreAuthorize("hasAuthority('lanyard_export')")
	public void exportEmployeeLanyardExport(HttpServletResponse response,  String empId, String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee_Lanyard_Management_" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportEmployeeLanyardManagement.fileExportBySearchValue(response,empId, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}

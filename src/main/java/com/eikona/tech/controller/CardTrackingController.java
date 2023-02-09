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
import com.eikona.tech.entity.CardTracking;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.export.ExportCardTrackingUtil;
import com.eikona.tech.export.ExportEmployeeCardManagement;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.service.CardTrackingService;
import com.eikona.tech.service.EmployeeService;

@Controller
public class CardTrackingController {
	
	@Autowired
	private CardTrackingService cardTrackingService;
	
	@Autowired
	private EmployeeService employeeService;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ExportCardTrackingUtil exportCardTrackingUtil;
	
	@Autowired
	private ExportEmployeeCardManagement exportEmployeeCardManagement;
	
	@GetMapping(value={"/card-tracking"})
	@PreAuthorize("hasAuthority('card_tracking_view')")
	public String employeeList(Model model) {
		return "cardTracking/card_tracking_list";
	}
	
	@GetMapping("/card-tracking/new")
	@PreAuthorize("hasAuthority('card_tracking_create')")
	public String newCard(Model model) {
		
		model.addAttribute("listEmployee", employeeService.getAll());
		model.addAttribute("cardTracking", new CardTracking());
		model.addAttribute("title", "New Card Management");
		return "cardTracking/card_tracking_new";
	}
	
	@GetMapping("/employee-card-tracking-view/{empId}")
	@PreAuthorize("hasAuthority('card_tracking_view')")
	public String addEmployeeCardTrackingView(@PathVariable(value = "empId") String empId, Model model) {
		model.addAttribute("title", "Card Management");
		model.addAttribute("empId", empId);
		model.addAttribute("redirect", "/employee/view/"+empId);
		return "cardTracking/card_tracking_list_view";
	}
	
	
	@GetMapping("/employee-card-tracking/{empId}")
	@PreAuthorize("hasAuthority('card_tracking_create')")
	public String addEmployeeCardTracking(@PathVariable(value = "empId") String empId, Model model) {
		String firstName="";
		String lastName="";
		Employee employeeObj = employeeRepository.findByEmployeeId(empId);
		if(null!=employeeObj.getFirstName())
			firstName=employeeObj.getFirstName();
		if(null!=employeeObj.getLastName())
			lastName=employeeObj.getLastName();
		model.addAttribute("cardTracking", new CardTracking());
	    model.addAttribute("employee", employeeObj);
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", employeeObj.getEmployeeId());
		model.addAttribute("title", "New Card Management");
		model.addAttribute("redirect", "/employee-card-tracking-view/");

		return "cardTracking/card_tracking_new";
	}
	
	@PostMapping("/card-tracking/add")
	@PreAuthorize("hasAnyAuthority('card_tracking_create','card_tracking_update')")
	public String saveArea(@ModelAttribute("cardTracking") CardTracking cardTracking,String redirectUrl) {
		
			cardTrackingService.save(cardTracking);
			if("/employee-card-tracking-view/".equalsIgnoreCase(redirectUrl))
			    return "redirect:"+redirectUrl+cardTracking.getEmployee().getEmployeeId();
			else
			    return "redirect:"+redirectUrl;
		}

	
	@GetMapping("/card-tracking/edit/{id}")
	@PreAuthorize("hasAuthority('card_tracking_update')")
	public String updateCard(@PathVariable(value = "id") long id, Model model) {
		CardTracking cardTracking = cardTrackingService.getById(id);
		String firstName="";
		String lastName="";
		Employee employeeObj = cardTracking.getEmployee();
		if(null!=employeeObj.getFirstName())
			firstName=employeeObj.getFirstName();
		if(null!=employeeObj.getLastName())
			lastName=employeeObj.getLastName();
	    model.addAttribute("employee", employeeObj);
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", employeeObj.getEmployeeId());
		model.addAttribute("cardTracking", cardTracking);
		model.addAttribute("title", "Update Card Management");
		model.addAttribute("redirect", "/card-tracking");
		return "cardTracking/card_tracking_new";
	}
	
	@GetMapping("/card-tracking-view/edit/{id}")
	@PreAuthorize("hasAuthority('card_tracking_update')")
	public String editCardManagement(@PathVariable(value = "id") long id, Model model) {
		CardTracking cardTracking = cardTrackingService.getById(id);
		String firstName="";
		String lastName="";
		Employee employeeObj = cardTracking.getEmployee();
		if(null!=employeeObj.getFirstName())
			firstName=employeeObj.getFirstName();
		if(null!=employeeObj.getLastName())
			lastName=employeeObj.getLastName();
		model.addAttribute("cardTracking", new CardTracking());
	    model.addAttribute("employee", employeeObj);
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", employeeObj.getEmployeeId());
		model.addAttribute("cardTracking", cardTracking);
		model.addAttribute("title", "Update Card Management");
		model.addAttribute("redirect", "/employee-card-tracking-view/");
		return "cardTracking/card_tracking_edit";
	}


	@RequestMapping(value = "/search/card-tracking", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('card_tracking_view')")
	public @ResponseBody PaginationDto<CardTracking> search(String cardId, String employee,  String type, String sDate,String eDate, int pageno, String sortField,
			String sortDir) {

		PaginationDto<CardTracking> dtoList = cardTrackingService.searchByField(cardId, employee, type, sDate,eDate, pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value = "/card-tracking-view/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('card_tracking_view')")
	public @ResponseBody PaginationDto<CardTracking> searchView(Long empId, int pageno, String sortField, String sortDir) {
		
		PaginationDto<CardTracking> dtoList = cardTrackingService.searchByField(String.valueOf(empId),pageno, sortField, sortDir);
		return dtoList;
	}
	@GetMapping(value = "/card-tracking/export-to-file")
	@PreAuthorize("hasAuthority('card_tracking_export')")
	public void exportCardTrackingReport(HttpServletResponse response, String cardId, String employee, String type, String sDate,String eDate,String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportCardTrackingUtil.fileExportBySearchValue(response, cardId, employee, type, sDate,eDate, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping(value = "/employee-card-management/export-to-file")
	@PreAuthorize("hasAuthority('card_tracking_export')")
	public void exportEmployeeCardManagementReport(HttpServletResponse response,String empId,String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee_Card_Management_" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportEmployeeCardManagement.fileExportBySearchValue(response, empId, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}

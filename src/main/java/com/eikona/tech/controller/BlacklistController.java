package com.eikona.tech.controller;

import java.text.DateFormat;
import java.text.ParseException;
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

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Blacklist;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.export.ExportSuspensionBlacklist;
import com.eikona.tech.export.ExportSuspensionUtil;
import com.eikona.tech.repository.BlacklistRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.service.BlacklistService;

@Controller
public class BlacklistController {
	
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private BlacklistService blacklistService;
	
	@Autowired
	private BlacklistRepository blacklistRepository;
	
	@Autowired
	private ExportSuspensionUtil exportSuspensionUtil;
	
	@Autowired
	private ExportSuspensionBlacklist exportSuspensionBlacklist;
	
	@GetMapping(value="/blacklist")
	@PreAuthorize("hasAuthority('blacklist_view')")
	public String blacklist(Model model) {
		return "blacklist/blacklist_list";
	}
	
	@GetMapping(value="/suspension")
	@PreAuthorize("hasAuthority('suspension_view')")
	public String suspension(Model model) {
		return "blacklist/suspension_list";
	}
	
	@RequestMapping(value = "/blacklist/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('blacklist_view')")
	public @ResponseBody PaginationDto<Blacklist> searchBlacklist(String employeeId,String orderBy, int pageno, String sortField, String sortDir) {
		
		PaginationDto<Blacklist> dtoList = blacklistService.searchByField(employeeId,orderBy,pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value = "/suspension/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('suspension_view')")
	public @ResponseBody PaginationDto<Blacklist> searchSuspension(String employeeId,String orderBy, int pageno, String sortField, String sortDir) {
		
		PaginationDto<Blacklist> dtoList = blacklistService.searchByField(employeeId,"Suspended",orderBy,pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value = "/blacklist-view/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('blacklist_view')")
	public @ResponseBody PaginationDto<Blacklist> searchBlacklistView(Long employeeId, int pageno, String sortField, String sortDir) {
		
		PaginationDto<Blacklist> dtoList = blacklistService.searchByField(String.valueOf(employeeId),pageno, sortField, sortDir);
		return dtoList;
	}
	
	@PostMapping("/employee-blacklist/add")
	@PreAuthorize("hasAnyAuthority('blacklist_create','blacklist_update')")
	public String saveEmployeeBlacklist(@ModelAttribute("blacklist") Blacklist blacklist,String redirectUrl,Model model) {
		blacklist.getEmployee().setStatus("Blacklisted");
		blacklist.setStatus("Blacklisted");
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		try {
		    blacklist.setStartDate(dateFormat.parse(blacklist.getStartDateStr()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return saveAndRedirect(blacklist, redirectUrl);
		
	}
	
	@PostMapping("/employee-suspension/add")
	@PreAuthorize("hasAnyAuthority('blacklist_create','blacklist_update')")
	public String saveEmployeeSuspension(@ModelAttribute("blacklist") Blacklist blacklist,String redirectUrl,Model model) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		blacklist.getEmployee().setStatus("Suspended");
		blacklist.setStatus("Suspended");
		if(!blacklist.getEndDateStr().isEmpty()) {
			try {
				    blacklist.setStartDate(dateFormat.parse(blacklist.getStartDateStr()));
					blacklist.setEndDate(dateFormat.parse(blacklist.getEndDateStr()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Long count = blacklistRepository.findByDateAndEmpIdCustom(blacklist.getEmployee().getEmployeeId(), blacklist.getStartDate(), blacklist.getEndDate());
				if(null==blacklist.getId()) {
					setNewBlacklist(blacklist, redirectUrl, model);
					if(blacklist.getStartDate().after(blacklist.getEndDate())) {
						 model.addAttribute("alert", "Start date is greater than End date");
						return "blacklist/employee_suspension_new";
					}
					else if(count != 0) {
						model.addAttribute("alert", "Employee is already Suspended within this date!");
						return "blacklist/employee_suspension_new";
					}
					else {
						return saveAndRedirect(blacklist, redirectUrl);
					}
					
				}else{
					if(blacklist.getRemovalDate().isEmpty()) {
						setEditBlacklist(blacklist, redirectUrl, model);
						if(blacklist.getStartDate().after(blacklist.getEndDate())) {
							 model.addAttribute("alert", "Start date is greater than End date");
							 if("/employee-suspension-blacklist-view/".equalsIgnoreCase(redirectUrl))
								 return "blacklist/employee_suspension_new";
							 else
								 return "blacklist/employee_suspension_edit";
						}
							  
						else if(count != 0) {
							 model.addAttribute("alert", "Employee is already Suspended within this date!");
							 if("/employee-suspension-blacklist-view/".equalsIgnoreCase(redirectUrl))
								 return "blacklist/employee_suspension_new";
							 else
								 return "blacklist/employee_suspension_edit";
						}
							 
						else {
							return saveAndRedirect(blacklist, redirectUrl);
						}
					}else {
						return saveAndRedirect(blacklist, redirectUrl);
					}
						
					}
		}else {
			try {
			    blacklist.setStartDate(dateFormat.parse(blacklist.getStartDateStr()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
			return saveAndRedirect(blacklist, redirectUrl);
		}
		
				
			}

	private String saveAndRedirect(Blacklist blacklist, String redirectUrl) {
		blacklistService.save(blacklist);
		
		if("/employee-suspension-blacklist-view/".equalsIgnoreCase(redirectUrl))
		    return "redirect:"+redirectUrl+blacklist.getEmployee().getEmployeeId();
		else
		    return "redirect:"+redirectUrl;
	}
			
			

	private void setEditBlacklist(Blacklist blacklist, String redirectUrl, Model model) {
		String firstName="";
		String lastName="";
		if(null!=blacklist.getEmployee().getFirstName())
			firstName=blacklist.getEmployee().getFirstName();
		if(null!=blacklist.getEmployee().getLastName())
			lastName=blacklist.getEmployee().getLastName();
		 model.addAttribute("blacklist", blacklist);
		model.addAttribute("employee", blacklist.getEmployee());
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", blacklist.getEmployee().getEmployeeId());
		model.addAttribute("title", "Update Suspension");
		model.addAttribute("redirect", redirectUrl);
	}

	private void setNewBlacklist(Blacklist blacklist, String redirectUrl, Model model) {
		String firstName="";
		String lastName="";
		Employee employeeObj = employeeRepository.findByEmployeeId(blacklist.getEmployee().getEmployeeId());
		if(null!=employeeObj.getFirstName())
			firstName=employeeObj.getFirstName();
		if(null!=employeeObj.getLastName())
			lastName=employeeObj.getLastName();
		 model.addAttribute("blacklist", new Blacklist());
		model.addAttribute("employee", employeeObj);
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", employeeObj.getEmployeeId());
		model.addAttribute("title", "Add Employee To Suspension");
		model.addAttribute("redirect", redirectUrl);
	}
		
		
			
	
	@GetMapping("/employee-suspension-blacklist-view/{empId}")
	@PreAuthorize("hasAuthority('suspension_view')")
	public String addEmployeeToBlacklistView(@PathVariable(value = "empId") String empId, Model model) {
		model.addAttribute("title", "Suspension & Blacklist");
		model.addAttribute("empId", empId);
		model.addAttribute("redirect", "/employee/view/"+empId);
		return "blacklist/suspension_blacklist_list_view";
	}
	
	@GetMapping("/employee-blacklist/{empId}")
	@PreAuthorize("hasAuthority('blacklist_create')")
	public String addEmployeeToBlacklist(@PathVariable(value = "empId") String empId, Model model) {
		String firstName="";
		String lastName="";
		Employee employeeObj = employeeRepository.findByEmployeeId(empId);
		if(null!=employeeObj.getFirstName())
			firstName=employeeObj.getFirstName();
		if(null!=employeeObj.getLastName())
			lastName=employeeObj.getLastName();
		 model.addAttribute("blacklist", new Blacklist());
	    model.addAttribute("employee", employeeObj);
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", employeeObj.getEmployeeId());
		model.addAttribute("title", "Add Employee To Blacklist");
		model.addAttribute("redirect", "/employee-suspension-blacklist-view/");

		return "blacklist/employee_blacklist_new";
	}
	@GetMapping("/employee-suspension/{empId}")
	@PreAuthorize("hasAuthority('blacklist_create')")
	public String addEmployeeToSuspension(@PathVariable(value = "empId") String empId, Model model) {
		String firstName="";
		String lastName="";
		Employee employeeObj = employeeRepository.findByEmployeeId(empId);
		if(null!=employeeObj.getFirstName())
			firstName=employeeObj.getFirstName();
		if(null!=employeeObj.getLastName())
			lastName=employeeObj.getLastName();
		 model.addAttribute("blacklist", new Blacklist());
	    model.addAttribute("employee", employeeObj);
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", employeeObj.getEmployeeId());
		model.addAttribute("title", "Add Employee To Suspension");
		model.addAttribute("redirect", "/employee-suspension-blacklist-view/");

		return "blacklist/employee_suspension_new";
	}
	
	@GetMapping("/employee-suspension-edit/{id}")
	@PreAuthorize("hasAuthority('blacklist_update')")
	public String editBlacklist(@PathVariable(value = "id") long id, Model model) {
		String firstName="";
		String lastName="";
		Blacklist blacklist = blacklistRepository.findById(id).get();
		if(null!=blacklist.getEmployee().getFirstName())
			firstName=blacklist.getEmployee().getFirstName();
		if(null!=blacklist.getEmployee().getLastName())
			lastName=blacklist.getEmployee().getLastName();
		 model.addAttribute("blacklist", blacklist);
	    model.addAttribute("employee", blacklist.getEmployee());
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", blacklist.getEmployee().getEmployeeId());
		model.addAttribute("title", "Update Suspension");
		model.addAttribute("redirect", "/suspension");

		return "blacklist/employee_suspension_edit";
	}
	
	@GetMapping("/employee-suspension-view-edit/{id}")
	@PreAuthorize("hasAuthority('blacklist_update')")
	public String updateBlacklist(@PathVariable(value = "id") long id, Model model) {
		String firstName="";
		String lastName="";
		Blacklist blacklist = blacklistRepository.findById(id).get();
		if(null!=blacklist.getEmployee().getFirstName())
			firstName=blacklist.getEmployee().getFirstName();
		if(null!=blacklist.getEmployee().getLastName())
			lastName=blacklist.getEmployee().getLastName();
		 model.addAttribute("blacklist", blacklist);
	    model.addAttribute("employee", blacklist.getEmployee());
		model.addAttribute("name", firstName+" "+lastName);
		model.addAttribute("empId", blacklist.getEmployee().getEmployeeId());
		model.addAttribute("title", "Update Suspension");
		model.addAttribute("redirect", "/employee-suspension-blacklist-view/");
		return "blacklist/employee_suspension_new";
	}
	@GetMapping(value = "/blacklist/export-to-file")
	@PreAuthorize("hasAuthority('blacklist_export')")
	public void exportBlacklist(HttpServletResponse response, String employee, String orderBy,String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Blacklist" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportSuspensionUtil.fileExportBySearchValue(response,employee, orderBy,"Blacklisted", flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	@GetMapping(value = "/suspension/export-to-file")
	@PreAuthorize("hasAuthority('suspension_export')")
	public void exportSuspension(HttpServletResponse response, String employee, String orderBy,String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Suspension" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportSuspensionUtil.fileExportBySearchValue(response,employee, orderBy,"Suspended", flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping(value = "/suspension-blacklist/export-to-file")
	@PreAuthorize("hasAuthority('suspension_export')")
	public void exportSuspensionBlacklist(HttpServletResponse response,String empId,String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Suspension_&_Blacklist_" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportSuspensionBlacklist.fileExportBySearchValue(response,empId, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}

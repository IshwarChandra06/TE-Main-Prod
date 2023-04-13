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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Transaction;
import com.eikona.tech.export.ExportAccessLogReport;
import com.eikona.tech.repository.EmployeeTypeRepository;
import com.eikona.tech.service.TransactionService;

@Controller
public class TransactionController {
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private ExportAccessLogReport exportAccessLogReport; 
	
	@Autowired
	private EmployeeTypeRepository employeeTypeRepository;
	
	
	@GetMapping("/transaction")
	@PreAuthorize("hasAuthority('access_log_view')")
	public String transactionList(Model model) {
		model.addAttribute("listEmployeeType", employeeTypeRepository.findAllNameCustom());
		return "transaction/transaction_list";
	}
	
	@RequestMapping(value = "/search/transaction", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('access_log_view')")
	public @ResponseBody PaginationDto<Transaction> searchVehicleLog(String sDate,String eDate, String employeeId, String employeeName, String employeeType,
			String department, String designation, String device,int pageno, String sortField, String sortDir) {
		
		PaginationDto<Transaction> dtoList = transactionService.searchByField(sDate, eDate, employeeId, employeeName, employeeType.trim(),department,designation,device, pageno, sortField, sortDir);
		
		return dtoList;
	}
	
	@RequestMapping(value = "access-logs/export-to-excel", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('access_log_export')")
	public void exportToFile(HttpServletResponse response, String sDate,String eDate, String employeeId,  String employeeName,String employeeType,String department, String designation, String device,String flag) {
		response.setContentType("application/octet-stream");
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		String currentDateTime = dateFormat.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Event_Report_" + currentDateTime + "." + flag;
		response.setHeader(headerKey, headerValue);
		try {
			exportAccessLogReport.fileExportBySearchValue(response,sDate,eDate, employeeId, employeeName,employeeType.trim(),department,designation,device,flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}

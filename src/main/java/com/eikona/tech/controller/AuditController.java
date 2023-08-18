package com.eikona.tech.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Audit;
import com.eikona.tech.export.ExportAuditLog;
import com.eikona.tech.service.AuditService;

@Controller
public class AuditController {
	
	@Autowired
	private AuditService auditService;
	
	@Autowired
	private ExportAuditLog exportAuditLog;

	@GetMapping("/audit-log")
	@PreAuthorize("hasAuthority('email_log_view')")
	public String auditLogList() {
		return "auditLog/audit_log_list";
	}
	
	@RequestMapping(value = "/audit-log/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('email_log_view')")
	public @ResponseBody PaginationDto<Audit> searchEmployee(String sDate,String eDate, String employeeId,String firstName,String lastName,
			String activity,int pageno, String sortField, String sortDir) {
		
		PaginationDto<Audit> dtoList = auditService.searchByField(sDate,eDate, employeeId,firstName,lastName,activity,pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value="/audit-log/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('email_log_export')")
	public void exportToFile(HttpServletResponse response,String sDate,String eDate, String employeeId,String firstName,String lastName,
			String activity,String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Device_" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportAuditLog.fileExportBySearchValue(response,sDate,eDate, employeeId,firstName,lastName,activity,flag );
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}

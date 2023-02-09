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
import com.eikona.tech.entity.EmailLogs;
import com.eikona.tech.export.ExportEmailLog;
import com.eikona.tech.service.EmailLogService;

@Controller
public class EmailLogController {
	
	@Autowired
	private EmailLogService emailLogService;
	
	@Autowired
	private ExportEmailLog exportEmailLog;
	
	@GetMapping("/email-log")
	@PreAuthorize("hasAuthority('email_log_view')")
	public String emailLogList() {
		return "emailLog/email_log_list";
	}
	
	@RequestMapping(value = "/email-log/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('email_log_view')")
	public @ResponseBody PaginationDto<EmailLogs> searchEmployee(String sDate,String eDate, String mailId,String type, int pageno, String sortField, String sortDir) {
		
		PaginationDto<EmailLogs> dtoList = emailLogService.searchByField(sDate,eDate, mailId,type,pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value="/email-log/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('device_export')")
	public void exportToFile(HttpServletResponse response,String sDate,String eDate, String mailId,String type,String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Device_" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportEmailLog.fileExportBySearchValue(response,sDate,eDate, mailId,type,flag );
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}

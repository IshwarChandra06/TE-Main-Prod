package com.eikona.tech.controller;

import java.security.Principal;
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
import com.eikona.tech.entity.DailyReport;
import com.eikona.tech.export.ExportWorkStatusReport;
import com.eikona.tech.service.WorkStatusReportService;

@Controller
public class WorkStatusReportController {
	
	@Autowired
	private WorkStatusReportService dailyReportService;
	
	@Autowired
	private ExportWorkStatusReport exportDailyReports;
	
	@GetMapping("/work-status-reports")
	@PreAuthorize("hasAuthority('work_status_report_view')")
	public String viewHomePage(Model model) {
		return "reports/work_status_report";
	}

	@RequestMapping(value = "/generate/daily-reports", method = RequestMethod.GET)
	public String generateDailyReportsPage() {
		return "reports/generate_daily_report";
	}
	
	@RequestMapping(value = "/search/work-reports", method = RequestMethod.GET)
//	@PreAuthorize("hasAuthority('work_status_report_view')")
	public @ResponseBody PaginationDto<DailyReport> search(String sDate,String eDate, String employeeId, String employeeName, String department, String designation,
			String employeeType, String workHour, int pageno, String sortField, String sortDir, Principal principal) {
		
		PaginationDto<DailyReport> dtoList = dailyReportService.searchByField(sDate, eDate, employeeId, employeeName, department, designation, employeeType, workHour, pageno, sortField, sortDir);
		
		return dtoList;
	}
	
	
	@RequestMapping(value="/work-status/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('work_status_report_export')")
	public void exportToFile(HttpServletResponse response, String sDate, String eDate, String employeeName, String employeeId, 
			String designation, String department, String employeeType, String workHour, String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Daily_Report" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportDailyReports.fileExportBySearchValue(response,sDate, eDate, employeeName,employeeId, designation, department, employeeType, workHour, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}

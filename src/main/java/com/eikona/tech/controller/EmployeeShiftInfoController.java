package com.eikona.tech.controller;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.EmployeeShiftInfoDto;
import com.eikona.tech.dto.PaginatedDto;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.dto.SearchRequestDto;
import com.eikona.tech.entity.EmployeeShiftInfo;
import com.eikona.tech.export.ExportEmployeeShiftInfo;
import com.eikona.tech.export.ExportMonthlyRoster;
import com.eikona.tech.service.EmployeeShiftInfoService;
import com.eikona.tech.service.impl.EmployeeShiftInfoServiceImpl;


@Controller
public class EmployeeShiftInfoController {
	
	@Autowired
	private EmployeeShiftInfoService employeeShiftInfoService;
	
	@Autowired
	private EmployeeShiftInfoServiceImpl employeeShiftInfoServiceImpl; 
	
	@Autowired
	private ExportEmployeeShiftInfo exportEmployeeShiftInfo;
	
	@Autowired
	private ExportMonthlyRoster exportMonthlyRoster;
	
	@RequestMapping(value = "/sync/employee-shift-info-from-sf", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_roster_view')")
	public @ResponseBody void syncEmployeeShiftInfoFromSF() {
		employeeShiftInfoServiceImpl.syncEmployeeShiftInfoListFromSAP();
	}
	@RequestMapping(value = "/edit/employee-shift-info-from-sf", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_roster_view')")
	public @ResponseBody void updateEmployeeShiftInfoFromSF() {
		employeeShiftInfoServiceImpl.updateEmployeeShiftInfoListFromSAP();
	}
	
	@GetMapping("/employee-shift-association")
	@PreAuthorize("hasAuthority('employee_roster_view')")
	public String employeeShiftDailyAssociation(Model model) {
		return "employeeshift/employee_shift_list";
	}
	
	@RequestMapping(value = "/employee-shift-association/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_roster_view')")
	public @ResponseBody PaginationDto<EmployeeShiftInfo> searchEmployeeShiftInfo(String sDate,String eDate, String employeeId, String employeeName,String department, String shift,
			int pageno, String sortField, String sortDir) {
		
		PaginationDto<EmployeeShiftInfo> dtoList = employeeShiftInfoService.searchByField(sDate, eDate, employeeId, employeeName,department, shift,  pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value="/employee-shift/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_roster_export')")
	public void exportToFile(HttpServletResponse response,String sDate,String eDate, String employeeId, String employeeName,String department, String shift,
			String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee_Shift" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportEmployeeShiftInfo.fileExportBySearchValue(response,sDate,eDate,employeeId, employeeName, department,shift,
					flag );
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/api/employee-shift-association/search", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('employee_roster_view')")
	public @ResponseBody PaginatedDto<EmployeeShiftInfoDto> search(@RequestBody SearchRequestDto paginatedDto,
			Principal principal) {

		String message = "";
		String messageType = "";
		PaginatedDto<EmployeeShiftInfoDto> paginatedDtoList = null;
		List<EmployeeShiftInfoDto> employeeShiftInfoDtoList = new ArrayList<>();
		try {
			
		    int pageSize=paginatedDto.getPageSize();
		    if(10<pageSize)
		    	pageSize=10;
				
			Page<EmployeeShiftInfo> page = employeeShiftInfoService.searchByField(paginatedDto.getPageNo(),
					pageSize, paginatedDto.getSortField(), paginatedDto.getSortOrder(), paginatedDto,
					principal);
			List<EmployeeShiftInfo> employeeShiftInfoList = page.getContent();

			for (EmployeeShiftInfo employeeShiftInfo : employeeShiftInfoList) {
				EmployeeShiftInfoDto employeeShiftInfoDto = new EmployeeShiftInfoDto();
				employeeShiftInfoDto.setLastModifiedDate(employeeShiftInfo.getLastModifiedDate());
				employeeShiftInfoDto.setEmployeeId(employeeShiftInfo.getEmployee().getEmployeeId());
				employeeShiftInfoDto.setDayNo(employeeShiftInfo.getDay());
				employeeShiftInfoDto.setDate(employeeShiftInfo.getDate());
				employeeShiftInfoDto.setShiftName(employeeShiftInfo.getShift());
				employeeShiftInfoDto.setShiftStartTime(employeeShiftInfo.getStartTime());
				employeeShiftInfoDto.setShiftEndTime(employeeShiftInfo.getEndTime());
				employeeShiftInfoDto.setWorkScheduleExternalCode(employeeShiftInfo.getWorkScheduleExternalCode());
				employeeShiftInfoDto.setHoliday(employeeShiftInfo.isHoliday());
				employeeShiftInfoDto.setDayModel(employeeShiftInfo.getDayModel());

				employeeShiftInfoDtoList.add(employeeShiftInfoDto);

			}

			List<EmployeeShiftInfo> totalEmployeeShiftInfoList = employeeShiftInfoService.findAll();
			Page<EmployeeShiftInfo> totalPage = new PageImpl<EmployeeShiftInfo>(totalEmployeeShiftInfoList);
			message = "Success";
			messageType = "S";
			paginatedDtoList = new PaginatedDto<EmployeeShiftInfoDto>(employeeShiftInfoDtoList, page.getTotalPages(),
					page.getNumber() + 1, page.getSize(), page.getTotalElements(), totalPage.getTotalElements(),
					message, messageType);

		} catch (Exception e) {
			e.printStackTrace();
			return new PaginatedDto<EmployeeShiftInfoDto>(employeeShiftInfoDtoList, 0, 0, 0, 0, 0, "Failed", "E");
		}
		return paginatedDtoList;
	}
	
	@GetMapping("/monthly-employee-shift")
	@PreAuthorize("hasAuthority('employee_roster_view')")
	public String employeeMonthlyShift() {
		return "employeeshift/monthly_employee_shift";
	}
	

	@RequestMapping(value = "/monthly-employee-shift/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_roster_view')")
	public @ResponseBody PaginationDto<EmployeeShiftInfo> searchEmployeeShiftMonthlyInfo(String date, String employeeId, String employeeName,String department, String shift,
			int pageno, String sortField, String sortDir) {
		
		PaginationDto<EmployeeShiftInfo> dtoList = employeeShiftInfoService.searchByField(date, employeeId, employeeName,department, shift,  pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value="/monthly-employee-shift/export-to-file",method = RequestMethod.GET)
	//@PreAuthorize("hasAuthority('employee_roster_export')")
	@PreAuthorize("hasAuthority('employee_roster_view')")
	public void exportMonthlyEmployeeShiftInfo(HttpServletResponse response, String date, String employeeId, String employeeName,String department, String shift,
			String flag) {
		
		response.setContentType("application/octet-stream");
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		String currentDateTime = dateFormat.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Employee_Shift" + currentDateTime + "."+flag;
		response.setHeader(headerKey, headerValue);
		
		try {
			exportMonthlyRoster.fileExportBySearchValue(response, date, employeeId, employeeName, department, shift, flag );
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}
	




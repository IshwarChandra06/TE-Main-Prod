package com.eikona.tech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Shift;
import com.eikona.tech.service.ShiftService;
import com.eikona.tech.service.impl.ShiftServiceImpl;

@Controller
public class ShiftController {
	
	@Autowired
	private ShiftService shiftService;
	
	@Autowired
	private ShiftServiceImpl shiftServiceImpl;
	
	@GetMapping("/shift")
	public String shiftList(Model model) {
		return "shift/shift_list";
	}
	
	@RequestMapping(value = "/shift/search", method = RequestMethod.GET)
	public @ResponseBody PaginationDto<Shift> searchEmployee(Long id, String sDate, String day, String name, int pageno, String sortField, String sortDir) {
		
		PaginationDto<Shift> dtoList = shiftService.searchByField(id, name, sDate, day, pageno, sortField, sortDir);
		return dtoList;
	}
	
	
	@GetMapping("/sync/shift")
	public @ResponseBody String shiftSync() {
		shiftServiceImpl.syncShiftListFromSap();
		return "";
	}
}

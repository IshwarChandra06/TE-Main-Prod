package com.eikona.tech.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.eikona.tech.entity.CardTracking;
import com.eikona.tech.service.impl.LostAndDamageCardCardTrackingServiceImpl;

@Controller
public class LostAndDamageCardTrackingController {
	
	@Autowired
	private LostAndDamageCardCardTrackingServiceImpl cardTrackingServiceImpl;
	
	
	
	@GetMapping(value={"/lost-card-tracking"})
	@PreAuthorize("hasAuthority('lost_card_tracking_view')")
	public String lostCardTrackingList(Model model) {
		return "reports/lost_card_tracking_list";
	}

	@RequestMapping(value = "/search/lost-card-tracking", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('lost_card_tracking_view')")
	public @ResponseBody PaginationDto<CardTracking> searchLostCard(String cardId, String employee, String sDate,String eDate, int pageno, String sortField, String sortDir) {

		List<String> type = new ArrayList<>();
		type.add("Lost");
		PaginationDto<CardTracking> dtoList = cardTrackingServiceImpl.searchByField(cardId, employee, sDate,eDate, type, pageno, sortField, sortDir);
		return dtoList;
	}
	
	@GetMapping(value = "/lost-card-tracking/export-to-file")
	@PreAuthorize("hasAuthority('lost_card_tracking_export')")
	public void exportLostReport(HttpServletResponse response, String cardId, String employee, String sDate,String eDate, String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			List<String> type = new ArrayList<>();
			type.add("Lost");
			cardTrackingServiceImpl.fileExportBySearchValue(response, cardId, employee,sDate,eDate,type, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	
	
	@GetMapping(value={"/damage-card-tracking"})
	@PreAuthorize("hasAuthority('lost_card_tracking_view')")
	public String damageCardTrackingList(Model model) {
		return "reports/damage_card_tracking_list";
	}

	@RequestMapping(value = "/search/damage-card-tracking", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('damage_card_tracking_view')")
	public @ResponseBody PaginationDto<CardTracking> searchDamageCard(String cardId, String employee, String sDate,String eDate,  int pageno, String sortField, String sortDir) {

		List<String> cardStatus = new ArrayList<>();
			cardStatus.add("Damage");
		PaginationDto<CardTracking> dtoList = cardTrackingServiceImpl.searchByField(cardId, employee, sDate,eDate, cardStatus, pageno, sortField, sortDir);
		return dtoList;
	}
	
	@GetMapping(value = "/damage-card-tracking/export-to-file")
	@PreAuthorize("hasAuthority('damage_card_tracking_export')")
	public void exportDamageReport(HttpServletResponse response, String cardId, String employee, String sDate,String eDate, String flag) {

		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			List<String> cardStatus = new ArrayList<>();
			cardStatus.add("Damage");
			cardTrackingServiceImpl.fileExportBySearchValue(response, cardId, employee, sDate,eDate, cardStatus, flag);
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}

}

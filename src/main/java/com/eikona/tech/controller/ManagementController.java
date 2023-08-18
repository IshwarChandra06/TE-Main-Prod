package com.eikona.tech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eikona.tech.service.impl.ActiveEmployeeServiceImpl;
import com.eikona.tech.service.impl.InactiveEmployeeServiceImpl;

@RestController
public class ManagementController {
	
	@Autowired
	private ActiveEmployeeServiceImpl activeEmployeeServiceImpl;
	
	@Autowired
	private InactiveEmployeeServiceImpl inactiveEmployeeServiceImpl;
	
	
	@GetMapping("/push/inactive-employee-to-bs")
	public void fetchAllInactiveEmployeeFromSF() {
		inactiveEmployeeServiceImpl.removeAccessLevelOfInactiveEmployeeFromSF();
	}
	
	@GetMapping("/push/active-employee-to-bs")
	public void fetchAllActiveEmployeeFromSF() {
		activeEmployeeServiceImpl.updateAllActiveEmployeeFromSF();
	}

}

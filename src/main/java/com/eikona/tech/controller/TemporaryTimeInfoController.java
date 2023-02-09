package com.eikona.tech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eikona.tech.service.impl.TemporaryTimeInfoServiceImpl;

@RestController
public class TemporaryTimeInfoController {
	
	@Autowired
	private TemporaryTimeInfoServiceImpl temporaryTimeInfoServiceImpl;
	
	@RequestMapping(value = "/sync/temporary-time-info-from-sf", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_roster_view')")
	public @ResponseBody void syncTemporaryTimeInfoFromSF() {
		temporaryTimeInfoServiceImpl.syncTemporaryTimeInfoListFromSAP();
	}

}

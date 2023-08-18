package com.eikona.tech.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.repository.AccessLevelRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.util.BioSecurityServerUtil;

@Service
@EnableScheduling
public class SchedulerServiceImpl {
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private BioSecurityServerUtil bioSecurityServerUtil;
	
	@Autowired
	private AccessLevelRepository accessLevelRepository;

//	@Scheduled(cron = "0 0 2 * * ?")
	public void removeAccessLevelFromSeparatedEmployee() {
		List<Employee> employeeList=employeeRepository.findAllByStatus("Inactive");
		List<AccessLevel> accLevel=new ArrayList<AccessLevel>();
		for(Employee employee:employeeList) {
			employee.setAccessLevel(accLevel);
			employeeRepository.save(employee);
			try {
				 bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void checkAllEmployeeFromBiosecurityAPI(){
		try {
			List<Employee> employeeList=employeeRepository.findAllByIsDeletedFalse();
			
			for(Employee employee:employeeList) {
					JSONObject dataObject= bioSecurityServerUtil.getEmployeeFromBioSecurity(employee.getEmployeeId());
					if(null!=dataObject) {
						System.out.println(employee.getEmployeeId());
						String cardNo=(String) dataObject.get("cardNo");
						String accessLevelIds=(String) dataObject.get("accLevelIds");
						if(null!=accessLevelIds && !accessLevelIds.isEmpty()) {
							String[] splitByComma=accessLevelIds.split(",");
							List<AccessLevel> accesslevelList= new ArrayList<>();
							for(String accessId:splitByComma) {
								AccessLevel accesslevel = accessLevelRepository.findByAccessId(accessId);
								accesslevelList.add(accesslevel);
							}
							employee.setAccessLevel(accesslevelList);
						}
						employee.setCardId(cardNo);
						employeeRepository.save(employee);
			        }else {
			        	bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
			        }
			  }
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void checkAllCardNoEmptyEmployeeFromBiosecurityAPI(){
		try {
			List<Employee> employeeList=employeeRepository.findAllByIsDeletedFalseAndCardIdEmptyCustom();
			
			for(Employee employee:employeeList) {
					JSONObject dataObject= bioSecurityServerUtil.getEmployeeFromBioSecurity(employee.getEmployeeId());
					if(null!=dataObject) {
						System.out.println(employee.getEmployeeId());
						String cardNo=(String) dataObject.get("cardNo");
						String accessLevelIds=(String) dataObject.get("accLevelIds");
						if(null!=accessLevelIds && !accessLevelIds.isEmpty()) {
							String[] splitByComma=accessLevelIds.split(",");
							List<AccessLevel> accesslevelList= new ArrayList<>();
							for(String accessId:splitByComma) {
								AccessLevel accesslevel = accessLevelRepository.findByAccessId(accessId);
								accesslevelList.add(accesslevel);
							}
							employee.setAccessLevel(accesslevelList);
						}
						
						employee.setCardId(cardNo);
						
						employeeRepository.save(employee);
			        }else {
			        	bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
			        }
			  }
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void checkAllEmployeeFromSFAPI(){
		try {
			
			int top = NumberConstants.HUNDRED;
			int skip = NumberConstants.ZERO;
			
			Date currentDate= new Date();
			List<Employee> employeeList=employeeRepository.findAllByStatusAndIsDeletedFalse("Active");
			
			for(Employee employee:employeeList) {
					JSONObject dataObject= bioSecurityServerUtil.getEmployeeFromBioSecurity(employee.getEmployeeId());
					if(null!=dataObject) {
						System.out.println(employee.getEmployeeId());
						String cardNo=(String) dataObject.get("cardNo");
						String accessLevelIds=(String) dataObject.get("accLevelIds");
						String[] splitByComma=accessLevelIds.split(",");
						List<AccessLevel> accesslevelList= new ArrayList<>();
						for(String accessId:splitByComma) {
							AccessLevel accesslevel = accessLevelRepository.findByAccessId(accessId);
							accesslevelList.add(accesslevel);
						}
						employee.setCardId(cardNo);
						employee.setAccessLevel(accesslevelList);
						employeeRepository.save(employee);
			        }else {
			        	bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
			        }
			  }
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package com.eikona.tech.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Device;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.EmployeeType;
import com.eikona.tech.repository.AccessLevelRepository;
import com.eikona.tech.repository.DeviceRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.EmployeeTypeRepository;

@Component
public class EntityMap {

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private DeviceRepository doorRepository;
	
	@Autowired
	private AccessLevelRepository accessLevelRepository;
	
	@Autowired
	private EmployeeTypeRepository employeeTypeRepository;

	public Map<String, Employee> getEmployeeByEmpId() {
		List<Employee> employeeList = employeeRepository.findAllByIsDeletedFalse();
		Map<String, Employee> employeeMap = new HashMap<String, Employee>();

		for (Employee employee : employeeList) {
			employeeMap.put(employee.getEmployeeId(), employee);
		}
		return employeeMap;
	}
	
	public Map<String, Employee> getActiveEmployeeByEmpId() {
		List<Employee> employeeList = employeeRepository.findAllByIsDeletedFalseAndStatus("Active");
		Map<String, Employee> employeeMap = new HashMap<String, Employee>();

		for (Employee employee : employeeList) {
			employeeMap.put(employee.getEmployeeId(), employee);
		}
		return employeeMap;
	}
	
	public Map<String, Device> getDoorByDoorId() {
		List<Device> doorList = (List<Device>) doorRepository.findAll();
		Map<String, Device> doorMap = new HashMap<String, Device>();

		for (Device door : doorList) {
			doorMap.put(door.getDoorId(), door);
		}
		return doorMap;
	}
	
	public Map<String, AccessLevel> getAccessLevelByName() {
		List<AccessLevel> accessLevelList = (List<AccessLevel>) accessLevelRepository.findAll();
		Map<String, AccessLevel> accMap = new HashMap<String, AccessLevel>();

		for (AccessLevel accessLevel : accessLevelList) {
			accMap.put(accessLevel.getName(), accessLevel);
		}
		return accMap;
	}
	
	public Map<String, AccessLevel> getAccessLevelByAccessId() {
		List<AccessLevel> accessLevelList = (List<AccessLevel>) accessLevelRepository.findAll();
		Map<String, AccessLevel> accMap = new HashMap<String, AccessLevel>();

		for (AccessLevel accessLevel : accessLevelList) {
			accMap.put(accessLevel.getAccessId(), accessLevel);
		}
		return accMap;
	}
	public Map<String, EmployeeType> getEmployeeTypeByName() {
		List<EmployeeType> employeeTypeList = (List<EmployeeType>) employeeTypeRepository.findAllByIsDeletedFalse();
		Map<String, EmployeeType> empTypeMap = new HashMap<String, EmployeeType>();

		for (EmployeeType employeeType : employeeTypeList) {
			empTypeMap.put(employeeType.getName(), employeeType);
		}
		return empTypeMap;
	}
}

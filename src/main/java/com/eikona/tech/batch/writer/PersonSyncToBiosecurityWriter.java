package com.eikona.tech.batch.writer;

import java.util.Date;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.LastSyncStatus;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.LastSyncStatusRepository;
import com.eikona.tech.util.BioSecurityServerUtil;

@Component
public class PersonSyncToBiosecurityWriter implements ItemWriter<Employee> {
		
		private EmployeeRepository employeeRepository;
		
		 private LastSyncStatusRepository lastSyncStatusRepository;
		
		private BioSecurityServerUtil bioSecurityServerUtil;

		@Autowired
		public PersonSyncToBiosecurityWriter(BioSecurityServerUtil bioSecurityServerUtil,EmployeeRepository employeeRepository,LastSyncStatusRepository lastSyncStatusRepository) {
			super();
			this.bioSecurityServerUtil = bioSecurityServerUtil;
			this.employeeRepository=employeeRepository;
			this.lastSyncStatusRepository=lastSyncStatusRepository;
		}
		
		@Override
		public void write(List<? extends Employee> employeeList)  {
			try {
				
				Date currDate=	new Date();
				for(Employee employee:employeeList) {
					employee=employeeRepository.findById(employee.getId()).get();
					bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
				}
				LastSyncStatus lastSyncStatus= lastSyncStatusRepository.findByActivity("BS Employee Push");
				if(null!=lastSyncStatus)
					lastSyncStatus.setLastSyncTime(currDate);
				else {
					 lastSyncStatus= new LastSyncStatus();
					 lastSyncStatus.setLastSyncTime(currDate);
				     lastSyncStatus.setActivity("BS Employee Push");
				}
				lastSyncStatusRepository.save(lastSyncStatus);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
		}

}

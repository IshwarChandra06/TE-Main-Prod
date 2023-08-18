package com.eikona.tech.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.EmailSetupConstants;
import com.eikona.tech.entity.Blacklist;
import com.eikona.tech.entity.Device;
import com.eikona.tech.entity.EmailLogs;
import com.eikona.tech.entity.EmailSetup;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.EmployeeShiftInfo;
import com.eikona.tech.export.ExportEmployeeMasterData;
import com.eikona.tech.export.ExportEmployeeShiftInfo;
import com.eikona.tech.export.ExportMonthlyTransaction;
import com.eikona.tech.export.ExportSuspensionBlacklist;
import com.eikona.tech.repository.BlacklistRepository;
import com.eikona.tech.repository.DeviceRepository;
import com.eikona.tech.repository.EmailLogsRepository;
import com.eikona.tech.repository.EmailSetupRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.EmployeeShiftInfoRepository;
import com.eikona.tech.service.EmailSetupService;
import com.eikona.tech.util.CalendarUtil;

@Service
@EnableScheduling
public class SchedulerMailServiceImpl {
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private BlacklistRepository blacklistRepository;
	
	@Autowired
	private EmailSetupRepository emailSetupRepository;
	
	@Autowired
	private EmailLogsRepository emailLogsRepository;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private EmployeeShiftInfoRepository employeeShiftInfoRepository;
	
	@Autowired
	private ExportEmployeeMasterData exportEmployeeMasterData;
	
	@Autowired
	private ExportSuspensionBlacklist exportSuspensionBlacklist;
	
	@Autowired
	private ExportEmployeeShiftInfo exportEmployeeShiftInfo;
	
	@Autowired
	private EmailSetupService emailSetupService;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Value("${device.offline.minute}")
	private Long time;
	
	@Autowired
	private ExportMonthlyTransaction exportMonthlyTransaction;
	
	@Scheduled(cron = "0 0 22 * * WED")
	public void sendMailOfEmployeeShiftInfoAtWednesday() {
		EmailSetup emailSetup =  emailSetupRepository.findById(1l).get();
		EmailLogs emailLogs = new EmailLogs();
		emailLogs.setDate(new Date());
		emailLogs.setType(emailSetup.getSubject());
		emailLogs.setToEmailId(emailSetup.getTo());
		try {
			
			if("Active".equalsIgnoreCase(emailSetup.getStatus())) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String dateStr = format.format(new Date());
				Date startDate=calendarUtil.getConvertedDate(format.parse(dateStr),-2, 00, 00, 00);
				
				Date endDate=calendarUtil.getConvertedDate(format.parse(dateStr),4, 23, 59, 59);
				List<EmployeeShiftInfo> employeeShiftList=employeeShiftInfoRepository.findByDateCustom(startDate, endDate);
				String fileName=exportEmployeeShiftInfo.excelGenerator(employeeShiftList);
				String contentBody=EmailSetupConstants.EMPLOYEE_SHIFT_INFO.formatted(format.format(startDate),format.format(endDate));
				
				emailSetup.setSubject("SF Employee Shift Info ("+format.format(startDate)+" to "+format.format(endDate)+")");
				emailSetupService.sendEmailAsAttachment(fileName, emailSetup, contentBody);
				emailLogs.setStatus("Success");
				
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			emailLogs.setStatus("Failed");
		}
		emailLogsRepository.save(emailLogs);
	}
	
	@Scheduled(cron = "0 0 22 * * FRI")
	public void sendMailOfEmployeeShiftInfoAtFriday() {
		EmailSetup emailSetup =  emailSetupRepository.findById(1l).get();
		EmailLogs emailLogs = new EmailLogs();
		emailLogs.setDate(new Date());
		emailLogs.setType(emailSetup.getSubject());
		emailLogs.setToEmailId(emailSetup.getTo());
		try {
			if("Active".equalsIgnoreCase(emailSetup.getStatus())) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String dateStr = format.format(new Date());
				Date startDate=calendarUtil.getConvertedDate(format.parse(dateStr),3, 00, 00, 00);
				
				Date endDate=calendarUtil.getConvertedDate(format.parse(dateStr),9, 23, 59, 59);
				
				List<EmployeeShiftInfo> employeeShiftList=employeeShiftInfoRepository.findByDateCustom(startDate, endDate);
				String fileName=exportEmployeeShiftInfo.excelGenerator(employeeShiftList);
				String contentBody=EmailSetupConstants.EMPLOYEE_SHIFT_INFO.formatted(format.format(startDate),format.format(endDate));
				
				emailSetup.setSubject("SF Employee Shift Info ("+format.format(startDate)+" to "+format.format(endDate)+")");
				emailSetupService.sendEmailAsAttachment(fileName, emailSetup, contentBody);
				emailLogs.setStatus("Success");
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			emailLogs.setStatus("Failed");
		}
		emailLogsRepository.save(emailLogs);
	}
	
	@Scheduled(cron="0 0 1 * * *" )
	public void sendMailOnProfileCreationAndInactiveStatus() {
		EmailSetup emailSetup =  emailSetupRepository.findById(2l).get();
		
		EmailLogs emailLogs = new EmailLogs();
		emailLogs.setDate(new Date());
		emailLogs.setType(emailSetup.getSubject());
		emailLogs.setToEmailId(emailSetup.getTo());
		
		if("Active".equalsIgnoreCase(emailSetup.getStatus())) {
			try {
					Date date = new Date();
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					df.parse(df.format(date));
					Date endDate = calendarUtil.getNextOrPreviousDate(df.parse(df.format(date)), -1, 23, 59, 59);
					Date startDate = calendarUtil.getNextOrPreviousDate(df.parse(df.format(date)), -1, 0, 0, 0);
				
					List<Employee> createdEmployeelist=employeeRepository.findByCreatedDateCustom(startDate,endDate);
					List<Employee> inactiveEmployeelist=employeeRepository.findAllByStatusAndLastUpdatedTimeCustom(startDate,endDate,"Inactive");
					
					
			        String dateStr = df.format(startDate.getTime());
			        String createdProfileFileName =exportEmployeeMasterData.excelGenerator(createdEmployeelist,"Employee_Profile_Creation_");
			        String inactiveProfileFileName=exportEmployeeMasterData.excelGenerator(inactiveEmployeelist,"Employee_Profile_Inactive_");
					
					String contentBody=EmailSetupConstants.PROFILE_CREATION_AND_INACTIVE_PROFILE.formatted(dateStr);
					
					emailSetupService.sendEmailAsDualAttachment(createdProfileFileName,inactiveProfileFileName, emailSetup, contentBody);
					
					emailLogs.setStatus("Success");
				
			}catch (Exception e) {
				e.printStackTrace();
				emailLogs.setStatus("Failed");
			}
			emailLogsRepository.save(emailLogs);
		}
	}
	
	@Scheduled(cron="0 0/10 * * * ?" )
	public void sendOfflineDeviceStatus() {
		System.out.println(new Date());
		EmailSetup emailSetup =  emailSetupRepository.findById(3l).get();
			EmailLogs emailLogs = new EmailLogs();
			emailLogs.setDate(new Date());
			emailLogs.setType(emailSetup.getSubject());
			emailLogs.setToEmailId(emailSetup.getTo());
		if("Active".equalsIgnoreCase(emailSetup.getStatus())) {
			try {
				List<Device> deviceList = deviceRepository.findAllByIsDeletedFalse();
				List<String> deviceNameList = new ArrayList<>();
				String deviceName="";
				for(Device device:deviceList) {
					long diff = new Date().getTime() - device.getLastOnline().getTime();
					long diffMinutes = diff / (60 * 1000);
					if(diffMinutes>=time) 
						deviceNameList.add(device.getName());
				}
				for(int i=0;i<deviceNameList.size();i++) {
					deviceName+=(i+1)+". "+deviceNameList.get(i)+"\n";
				}
				
				if(!deviceName.isEmpty()) {
					System.out.println(deviceName);
					String contentBody=EmailSetupConstants.OFFLINE_DEVICE.formatted(deviceName);
		            emailSetupService.sendEmail(emailSetup, contentBody);
					emailLogs.setStatus("Success");
					emailLogsRepository.save(emailLogs);
				}
			}catch (Exception e) {
				e.printStackTrace();
				emailLogs.setStatus("Failed");
				emailLogsRepository.save(emailLogs);
			}
			
	  }
		
	}
	
//	@Scheduled(cron="0 0 1 * * *" )
	public void sendMailOnProfileCreation() {
		EmailSetup emailSetup =  emailSetupRepository.findById(4l).get();
		
		if("Active".equalsIgnoreCase(emailSetup.getStatus())) {
			try {
				Date date = new Date();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				df.parse(df.format(date));
				Date endDate = calendarUtil.getNextOrPreviousDate(df.parse(df.format(date)), -1, 23, 59, 59);
				Date startDate = calendarUtil.getNextOrPreviousDate(df.parse(df.format(date)), -1, 0, 0, 0);
				
				
				List<Employee> employeelist=employeeRepository.findByCreatedDateCustom(startDate,endDate);
				
				if(!employeelist.isEmpty()) {
					
					String dateStr = df.format(startDate.getTime());
					
					String fileName =exportEmployeeMasterData.excelGenerator(employeelist,"Employee_Profile_Creation_");
					String contentBody=EmailSetupConstants.PROFILE_CREATION.formatted(dateStr);
					
					emailSetupService.sendEmailAsAttachment(fileName, emailSetup, contentBody);
					
					EmailLogs emailLogs = new EmailLogs();
					emailLogs.setDate(new Date());
					emailLogs.setType(emailSetup.getSubject());
					emailLogs.setToEmailId(emailSetup.getTo());
					emailLogsRepository.save(emailLogs);
				}
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
//	@Scheduled(cron="0 10 1 * * *" )
	public void sendMailOnBlacklisting() {
		
		EmailSetup emailSetup =  emailSetupRepository.findById(5l).get();
		
		if("Active".equalsIgnoreCase(emailSetup.getStatus())) {
			try {
				Date date = new Date();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				df.parse(df.format(date));
				Date endDate = calendarUtil.getNextOrPreviousDate(df.parse(df.format(date)), -1, 23, 59, 59);
				Date startDate = calendarUtil.getNextOrPreviousDate(df.parse(df.format(date)), -1, 0, 0, 0);
				
				List<Blacklist> blacklistList = blacklistRepository.findByCreatedDateAndStatusCustom(startDate,endDate,"Blacklisted");
				
				if(!blacklistList.isEmpty()) {
					String fileName =exportSuspensionBlacklist.generateBlacklistExcel(blacklistList);
					
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String dateStr = format.format(startDate.getTime());
					
					String contentBody=EmailSetupConstants.BLACK_LISTING.formatted(dateStr);
					
					emailSetupService.sendEmailAsAttachment(fileName, emailSetup, contentBody);
					
					EmailLogs emailLogs = new EmailLogs();
					emailLogs.setDate(new Date());
					emailLogs.setType(emailSetup.getSubject());
					emailLogs.setToEmailId(emailSetup.getTo());
					emailLogsRepository.save(emailLogs);
				}
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
//	@Scheduled(cron="0 20 1 * * *" )
	public void sendMailOnSuspended() {

		EmailSetup emailSetup =  emailSetupRepository.findById(6l).get();
		
		if("Active".equalsIgnoreCase(emailSetup.getStatus())) {
			try {
				Date date = new Date();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				df.parse(df.format(date));
				Date endDate = calendarUtil.getNextOrPreviousDate(df.parse(df.format(date)), -1, 23, 59, 59);
				Date startDate = calendarUtil.getNextOrPreviousDate(df.parse(df.format(date)), -1, 0, 0, 0);
				
				List<Blacklist> blacklistList = blacklistRepository.findByCreatedDateAndStatusCustom(startDate,endDate,"Suspended");
				
				if(!blacklistList.isEmpty()) {
					String fileName =exportSuspensionBlacklist.generateSuspensionExcel(blacklistList);
					
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String dateStr = format.format(startDate.getTime());
					
					String contentBody=EmailSetupConstants.SUSPENSION.formatted(dateStr);
					
					emailSetupService.sendEmailAsAttachment(fileName, emailSetup, contentBody);
					
					EmailLogs emailLogs = new EmailLogs();
					emailLogs.setDate(new Date());
					emailLogs.setType(emailSetup.getSubject());
					emailLogs.setToEmailId(emailSetup.getTo());
					emailLogsRepository.save(emailLogs);
				}
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

//	@Scheduled(cron = "0 0 0 1 * *")
	public void sendMailOfMonthlyAccessReport() {
		EmailSetup emailSetup =  emailSetupRepository.findById(7l).get();
		
		if("Active".equalsIgnoreCase(emailSetup.getStatus())) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Calendar calender = Calendar.getInstance();
				calender.setTime(format.parse(format.format(new Date())));
				calender.add(Calendar.MONTH, -1);
				String currentDateTime = calender.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH )+" "+calender.get(Calendar.YEAR);
				String contentBody=EmailSetupConstants.MONTHLY_ACCESS_REPORT.formatted(currentDateTime);
				
				String fileName = exportMonthlyTransaction.excelGenerator(calender);
				emailSetupService.sendEmailAsAttachment(fileName, emailSetup, contentBody);
				
				EmailLogs emailLogs = new EmailLogs();
				emailLogs.setDate(new Date());
				emailLogs.setType(emailSetup.getSubject());
				emailLogs.setToEmailId(emailSetup.getTo());
				emailLogsRepository.save(emailLogs);
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}

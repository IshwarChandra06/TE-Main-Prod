package com.eikona.tech.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.apache.poi.util.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmailSetupConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.constants.SAPServerConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.dto.SearchRequestDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.EmailLogs;
import com.eikona.tech.entity.EmailSetup;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Image;
import com.eikona.tech.entity.LastSyncStatus;
import com.eikona.tech.repository.EmailLogsRepository;
import com.eikona.tech.repository.EmailSetupRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.ImageRepository;
import com.eikona.tech.repository.LastSyncStatusRepository;
import com.eikona.tech.service.EmployeeService;
import com.eikona.tech.util.BioSecurityServerUtil;
import com.eikona.tech.util.CalendarUtil;
import com.eikona.tech.util.EntityMap;
import com.eikona.tech.util.ExcelEmployeeImport;
import com.eikona.tech.util.GeneralSpecificationUtil;
import com.eikona.tech.util.ImageProcessingUtil;
import com.eikona.tech.util.RequestExecutionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@EnableScheduling
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
	private LastSyncStatusRepository lastSyncStatusRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Employee> generalSpecificationEmployee;

	@SuppressWarnings("rawtypes")
	@Autowired
	protected PaginatedServiceImpl paginatedServiceImpl;
	
	@Autowired
	private CalendarUtil calendarUtil;

	@Autowired
	private RequestExecutionUtil requestExecutionUtil;

	@Autowired
	private EntityMap employeeObjectMap;
	
	@Autowired
	private BioSecurityServerUtil bioSecurityServerUtil;
	
	@Autowired
	private ImageProcessingUtil imageProcessingUtil;

	@Value("${sap.login.username}")
	private String username;

	@Value("${sap.login.password}")
	private String password;
	
	@Autowired
	private ExcelEmployeeImport excelEmployeeImport;
	
	@Autowired
	private EmailSetupRepository emailSetupRepository;
	
	@Autowired
	private EmailSetupServiceImpl emailSetupServiceImpl;
	
	@Autowired
	private EmailLogsRepository emailLogsRepository;

	@Override
	public List<Employee> getAll() {
		return employeeRepository.findAllByIsDeletedFalse();
	}

	@Override
	public Employee save(Employee employee) {
		employee.setDeleted(false);
		employee.setSource("Manual");
		return this.employeeRepository.save(employee);

	}

	@Override
	public Employee getById(long id) {
		Optional<Employee> optional = employeeRepository.findById(id);
		Employee employee = null;
		if (optional.isPresent()) {
			employee = optional.get();
		} else {
			throw new RuntimeException(EmployeeConstants.EMPLOYEE_NOT_FOUND + id);
		}
		return employee;
	}

	@Override
	public void deleteById(long id, Principal principal) {
		Optional<Employee> optional = employeeRepository.findById(id);
		Employee employee = null;
		if (optional.isPresent()) {
			employee = optional.get();
			employee.setDeleted(true);
		} else {
			throw new RuntimeException(EmployeeConstants.EMPLOYEE_NOT_FOUND + id);
		}
		this.employeeRepository.save(employee);

	}
	
	public void syncEmployeeListFromSap() {
		try {
			int top = NumberConstants.HUNDRED;
			int skip = NumberConstants.ZERO;
			
			Date currentDate= new Date();
			
			while (true) {

				JSONArray resultsArray = getEmployeeListFromSap(top, skip);

				if (null != resultsArray && !resultsArray.isEmpty()) {
					List<Employee> employeeList = new ArrayList<Employee>();

					for (int i = NumberConstants.ZERO; i < resultsArray.size(); i++) {
						JSONObject currentObj = (JSONObject) resultsArray.get(i);
						Employee employee = new Employee();
						setEmployeeDetails(currentObj, employee);
						employee.setSource("SF");
						employeeList.add(employee);

					}
					skip += NumberConstants.HUNDRED;
					enrollEmployeeInMataFromSap(employeeList);
				} else
					break;

			}
			LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate,"SF Employee Sync");
			lastSyncStatusRepository.save(lastSyncStatus);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Scheduled(cron = "0 0 0/6 * * ?")
	public void updateEmployeeListFromSapByDateTime() {
		try {
			int top = NumberConstants.HUNDRED;
			int skip = NumberConstants.ZERO;
			
			Date currentDate= new Date();
			while (true) {

				JSONArray resultsArray = getEmployeeListFromSapByDateFilter(top, skip);

				if (null != resultsArray && !resultsArray.isEmpty()) {
					List<Employee> employeeList = new ArrayList<Employee>();

					for (int i = NumberConstants.ZERO; i < resultsArray.size(); i++) {
						JSONObject currentObj = (JSONObject) resultsArray.get(i);
						Employee employee = new Employee();
						setEmployeeDetails(currentObj, employee);
						employee.setSource("SF");
						employeeList.add(employee);

					}
					skip += NumberConstants.HUNDRED;
					updateEmployeeInMataFromSap(employeeList);
				} else
					break;

			}
			LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate,"SF Employee Sync");
			lastSyncStatusRepository.save(lastSyncStatus);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private LastSyncStatus setLastSyncStatus(Date currentDate,String activity) {
		LastSyncStatus lastSyncStatus = lastSyncStatusRepository.findByActivity(activity);
		if(null!=lastSyncStatus)
			lastSyncStatus.setLastSyncTime(currentDate);
		else {
			    lastSyncStatus = new LastSyncStatus();
				lastSyncStatus.setActivity(activity);
				lastSyncStatus.setLastSyncTime(currentDate);
		}
		return lastSyncStatus;
	}

	private void setEmployeeDetails(JSONObject currentObj, Employee employee) throws ParseException {
		
		    setEmployeeBasicInfo(currentObj, employee);	
		    
			setHireDateAndEndDate(currentObj, employee);

			setEmployeeStatus(currentObj, employee);	
			
			setManagerDetails(currentObj, employee);
				
			JSONObject userNav = (JSONObject) currentObj.get(SAPServerConstants.USER_NAV);
			if(null!=userNav) {
				setHostelDetails(userNav, employee);
				setBusDetails( userNav,employee);
			}
		

	}

	private void setBusDetails(JSONObject userNav,Employee employee) {
		JSONObject busServiceObj = (JSONObject) userNav.get(SAPServerConstants.BUS_SERVICE_NAV);
		if(null!=busServiceObj) {
			JSONArray busResults = (JSONArray) busServiceObj.get(SAPServerConstants.RESULTS);
			if (busResults.size() != 0) {
				JSONObject busObj = (JSONObject) busResults.get(0);
				employee.setNodalPoint((String) busObj.get(SAPServerConstants.NODAL_POINT));
				employee.setBusNo((String) busObj.get(SAPServerConstants.BUS_NO));
			}
		}
	}

	private void setHireDateAndEndDate(JSONObject currentObj, Employee employee) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US);
		String hiredate = (String) currentObj.get(SAPServerConstants.HIRE_DATE);
		String enddate = (String) currentObj.get(SAPServerConstants.END_DATE);
		String startdate = (String) currentObj.get(SAPServerConstants.START_DATE);
		
		if (null != hiredate) {
			Date joinDate = new Date(Long.valueOf(hiredate.substring(NumberConstants.SIX, NumberConstants.NINETEEN)));
			employee.setJoinDate(dateFormat.parse(dateFormat.format(joinDate)));
		}
		
		if (null != startdate) {
			Date startDate = new Date(Long.valueOf(startdate.substring(NumberConstants.SIX, NumberConstants.NINETEEN)));
			employee.setStartDate(dateFormat.parse(dateFormat.format(startDate)));
		}

		if (null != enddate) {
			Date endDate =null;
			try {
				endDate = new Date(Long.valueOf(enddate.substring(NumberConstants.SIX, NumberConstants.TWENTY_ONE)));
			}catch (Exception e) {
				endDate = new Date(Long.valueOf(enddate.substring(NumberConstants.SIX, NumberConstants.NINETEEN)));
			}
			employee.setEndDate(dateFormat.parse(dateFormat.format(endDate)));
		}
	}

	private void setEmployeeBasicInfo(JSONObject currentObj, Employee employee) {
		JSONObject employeementObj = (JSONObject) currentObj.get(SAPServerConstants.EMPLOYEEMENT_NAV);
		JSONObject employeeJobRelationshipObj = (JSONObject) employeementObj.get(SAPServerConstants.EMPLOYEE_JOB_RELATIONSHIP_NAV);
		JSONObject personObj = (JSONObject) employeementObj.get(SAPServerConstants.PERSON_NAV);
		JSONObject personalInfoObj = (JSONObject) personObj.get(SAPServerConstants.PERSONAL_INFO_NAV);
		JSONObject phoneInfoObj = (JSONObject) personObj.get(SAPServerConstants.PHONE_NAV);
		JSONObject emailInfoObj = (JSONObject) personObj.get(SAPServerConstants.EMAIL_NAV);
		JSONObject departmentObj = (JSONObject) currentObj.get(SAPServerConstants.DEPARTMENT_NAV);
		if (null != departmentObj)
			employee.setDepartment((String) departmentObj.get(SAPServerConstants.NAME));
		
		employee.setEmployeeId((String) currentObj.get(SAPServerConstants.USER_ID));
		employee.setManagerId((String) currentObj.get(SAPServerConstants.MANAGER_ID));
		employee.setDesignation((String) currentObj.get(SAPServerConstants.CUSTOM_STRING_2));
		employee.setCadre((String) currentObj.get(SAPServerConstants.CUSTOM_STRING_7));
		employee.setPayGrade((String) currentObj.get(SAPServerConstants.PAY_GRADE));
		
		JSONArray results = (JSONArray) personalInfoObj.get(SAPServerConstants.RESULTS);
		if (results.size() != 0) {
			JSONObject personalObj = (JSONObject) results.get(0);
			employee.setFirstName((String) personalObj.get(SAPServerConstants.FIRST_NAME));
			employee.setLastName((String) personalObj.get(SAPServerConstants.LAST_NAME));
		}
		
		setEmployeeContactInfo(employee, phoneInfoObj, emailInfoObj);
		
		setDepartmentHodUserId(employee, employeeJobRelationshipObj);
	}

	private void setDepartmentHodUserId(Employee employee, JSONObject employeeJobRelationshipObj) {
		JSONArray deptHodResults = (JSONArray) employeeJobRelationshipObj.get(SAPServerConstants.RESULTS);
		if (deptHodResults.size() != 0) {
			String relUserId="";
			for(int i=0;i<deptHodResults.size();i++) {
				JSONObject deptHodObj = (JSONObject) deptHodResults.get(i);
				String hodUserId= (String) deptHodObj.get(SAPServerConstants.REL_USER_ID);
				JSONObject relnTypeNav= (JSONObject) deptHodObj.get(SAPServerConstants.RELATIONSHIP_TYPE_NAV);
				String externalCode= (String) relnTypeNav.get(SAPServerConstants.EXTERNAL_CODE);
				if(relUserId.isEmpty())
					relUserId=hodUserId+" - "+externalCode;
				else
					relUserId=relUserId+","+hodUserId+" - "+externalCode;
			}
			employee.setRelUserId(relUserId);
		}
	}

	private void setEmployeeContactInfo(Employee employee, JSONObject phoneInfoObj, JSONObject emailInfoObj) {
		JSONArray phoneResults = (JSONArray) phoneInfoObj.get(SAPServerConstants.RESULTS);
		if (phoneResults.size() != 0) {
			JSONObject phoneObj = (JSONObject) phoneResults.get(0);
			employee.setContactNo((String) phoneObj.get(SAPServerConstants.PHONE_NO));
		}
		
		JSONArray emailResults = (JSONArray) emailInfoObj.get(SAPServerConstants.RESULTS);
		if (emailResults.size() != 0) {
			String emailId="";
			for(int i=0;i<emailResults.size();i++) {
				JSONObject emailObj = (JSONObject) emailResults.get(i);
				String email= (String) emailObj.get(SAPServerConstants.EMAIL_ADDRESS);
				if(emailId.isEmpty())
					emailId=email;
				else
					emailId=emailId+","+email;
			}
			employee.setEmailId(emailId);
		}
	}

	private void setEmployeeStatus(JSONObject currentObj, Employee employee) {
		JSONObject employeeStatusNav = (JSONObject) currentObj.get(SAPServerConstants.EMPLOYEE_STATUS_NAV);
		JSONObject pickLabelList = (JSONObject) employeeStatusNav.get(SAPServerConstants.PICK_LIST_LABEL);
		JSONArray statusResults = (JSONArray) pickLabelList.get(SAPServerConstants.RESULTS);
		if (statusResults.size() != 0) {
			JSONObject statusObj = (JSONObject) statusResults.get(0);
			if("Separated".equalsIgnoreCase((String) statusObj.get(SAPServerConstants.LABEL)))
				 employee.setStatus("Inactive");
			else
			     employee.setStatus((String) statusObj.get(SAPServerConstants.LABEL));
		}
	}

	private void setHostelDetails(JSONObject userNav, Employee employee) {
			JSONObject hostelFacilityObj = (JSONObject) userNav.get(SAPServerConstants.HOSTEL_FACILITY_NAV);
			if(null!=hostelFacilityObj) {
				JSONArray hostelResults = (JSONArray) hostelFacilityObj.get(SAPServerConstants.RESULTS);
				if (hostelResults.size() != 0) {
					JSONObject hostelObj = (JSONObject) hostelResults.get(0);
					employee.setHostelName((String) hostelObj.get(SAPServerConstants.HOSTEL_NAME));
					employee.setHostelWardenName((String) hostelObj.get(SAPServerConstants.WARDEN_NAME));
					employee.setHostelWardenEmail((String) hostelObj.get(SAPServerConstants.WARDEN_EMAIL));
					employee.setHostelWardenMobile((String) hostelObj.get(SAPServerConstants.WARDEN_MOBILE_NO));
				}
		}
	}

	private void setManagerDetails(JSONObject currentObj, Employee employee) {
		JSONObject managerNav = (JSONObject) currentObj.get(SAPServerConstants.MANAGER_EMPLOYMENT_NAV);
		if(null!=managerNav) {
			JSONObject managerPersonObj = (JSONObject) managerNav.get(SAPServerConstants.PERSON_NAV);
			JSONObject managerPersonalInfoObj = (JSONObject) managerPersonObj.get(SAPServerConstants.PERSONAL_INFO_NAV);
			JSONArray managerPersonalResults = (JSONArray) managerPersonalInfoObj.get(SAPServerConstants.RESULTS);
			if (managerPersonalResults.size() != 0) {
				JSONObject managerPersonalObj = (JSONObject) managerPersonalResults.get(0);
				String firstName=(String) managerPersonalObj.get(SAPServerConstants.FIRST_NAME);
				String middleName=(String) managerPersonalObj.get(SAPServerConstants.MIDDLE_NAME);
				String lastName=(String) managerPersonalObj.get(SAPServerConstants.LAST_NAME);
				String name="";
				if(null!=firstName)
					name+=firstName;
				if(null!=middleName)
					name+=" "+middleName;
				if(null!=lastName)
					name+=" "+lastName;
				employee.setManagerName(name);
			}
			JSONObject managerEmailObj = (JSONObject) managerPersonObj.get(SAPServerConstants.EMAIL_NAV);
			if(null!=managerEmailObj) {
				JSONArray emailResults = (JSONArray) managerEmailObj.get(SAPServerConstants.RESULTS);
				String emailId="";
				for(int i=0;i<emailResults.size();i++) {
					JSONObject emailObj = (JSONObject) emailResults.get(i);
					String email= (String) emailObj.get(SAPServerConstants.EMAIL_ADDRESS);
					if(emailId.isEmpty())
						emailId=email;
					else
						emailId=emailId+","+email;
				}
				employee.setManagerEmail(emailId);
			}
			}
	}

	public void enrollEmployeeInMataFromSap(List<Employee> employeeList) {
		Map<String, Employee> employeeMap = employeeObjectMap.getEmployeeByEmpId();
		List<Employee> savingList = new ArrayList<Employee>();
		for (Employee employee : employeeList) {
			Employee emp = employeeMap.get(employee.getEmployeeId());
			if (null == emp)
				savingList.add(employee);
			else {
				emp.setFirstName(employee.getFirstName());
				emp.setLastName(employee.getLastName());
				emp.setCreatedBy(employee.getCreatedBy());
				emp.setCreatedDate(employee.getCreatedDate());
				emp.setCadre(employee.getCadre());
				emp.setCardId(employee.getCardId());
				emp.setDepartment(employee.getDepartment());
				emp.setDesignation(employee.getDesignation());
				emp.setEndDate(employee.getEndDate());
				emp.setJoinDate(employee.getJoinDate());
				emp.setStartDate(employee.getStartDate());
				emp.setPayGrade(employee.getPayGrade());
				emp.setManagerId(employee.getManagerId());
				emp.setManagerName(employee.getManagerName());
				emp.setManagerEmail(employee.getManagerEmail());
				emp.setHostelName(employee.getHostelName());
				emp.setHostelWardenName(employee.getHostelWardenName());
				emp.setHostelWardenEmail(employee.getHostelWardenEmail());
				emp.setHostelWardenMobile(employee.getHostelWardenMobile());
				emp.setBusNo(employee.getBusNo());
				emp.setNodalPoint(employee.getNodalPoint());
				emp.setContactNo(employee.getContactNo());
				emp.setEmailId(employee.getEmailId());
				emp.setRelUserId(employee.getRelUserId());
				emp.setStatus(employee.getStatus());
				savingList.add(emp);
			}
		}
		employeeRepository.saveAll(savingList);

	}
	public void updateEmployeeInMataFromSap(List<Employee> employeeList) {
		Map<String, Employee> employeeMap = employeeObjectMap.getEmployeeByEmpId();
		List<Employee> savingList = new ArrayList<Employee>();
		for (Employee employee : employeeList) {
			Employee emp = employeeMap.get(employee.getEmployeeId());
			if (null == emp)
				savingList.add(employee);
			else {
				emp.setFirstName(employee.getFirstName());
				emp.setLastName(employee.getLastName());
				emp.setCreatedBy(employee.getCreatedBy());
				emp.setCreatedDate(employee.getCreatedDate());
				emp.setCadre(employee.getCadre());
				emp.setCardId(employee.getCardId());
				emp.setDepartment(employee.getDepartment());
				emp.setDesignation(employee.getDesignation());
				emp.setEndDate(employee.getEndDate());
				emp.setJoinDate(employee.getJoinDate());
				emp.setStartDate(employee.getStartDate());
				emp.setPayGrade(employee.getPayGrade());
				emp.setManagerId(employee.getManagerId());
				emp.setManagerName(employee.getManagerName());
				emp.setManagerEmail(employee.getManagerEmail());
				emp.setHostelName(employee.getHostelName());
				emp.setHostelWardenName(employee.getHostelWardenName());
				emp.setHostelWardenEmail(employee.getHostelWardenEmail());
				emp.setHostelWardenMobile(employee.getHostelWardenMobile());
				emp.setBusNo(employee.getBusNo());
				emp.setNodalPoint(employee.getNodalPoint());
				emp.setContactNo(employee.getContactNo());
				emp.setEmailId(employee.getEmailId());
				emp.setRelUserId(employee.getRelUserId());
				emp.setStatus(employee.getStatus());
				savingList.add(emp);
			}
		}
		employeeRepository.saveAll(savingList);

	}

	public JSONArray getEmployeeListFromSap(int top, int skip) {
		JSONArray resultsArray = new JSONArray();
		try {
			String myurl = SAPServerConstants.EMPLOYEE_MASTER_DATA_API.formatted(String.valueOf(top),
					String.valueOf(skip));
			String newurl = myurl.replaceAll(ApplicationConstants.DELIMITER_SPACE,
					ApplicationConstants.DELIMITER_FORMAT_SPACE);
			resultsArray = getEmployeeResultsArray(newurl);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultsArray;

	}
	
	public JSONArray getEmployeeListFromSapByDateFilter(int top, int skip) throws ParseException, Exception {
		    JSONArray resultsArray = new JSONArray();
		
			LastSyncStatus lastSyncStatus=lastSyncStatusRepository.findByActivity("SF Employee Sync");
			if(null!=lastSyncStatus) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US_SEPARATED_BY_T);
				
				String lastSyncTime=dateFormat.format(lastSyncStatus.getLastSyncTime())+"Z";
				String currentTime=dateFormat.format(new Date())+"Z";
				
				String myurl = SAPServerConstants.EMPLOYEE_MASTER_DATA_BY_DATE_API.formatted(lastSyncTime,currentTime,String.valueOf(top),
						String.valueOf(skip));
				String newurl = myurl.replaceAll(ApplicationConstants.DELIMITER_SPACE,
						ApplicationConstants.DELIMITER_FORMAT_SPACE);
				resultsArray = getEmployeeResultsArray(newurl);
			}
		
		return resultsArray;

	}

	private JSONArray getEmployeeResultsArray(String newurl) throws Exception, ParseException {
		JSONArray resultsArray =new JSONArray();
		HttpGet request = getSAPGetRequest(newurl);
		String responeData = requestExecutionUtil.executeHttpGetRequest(request);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONObject firstObj = (JSONObject) jsonResponse.get(SAPServerConstants.D);
			resultsArray = (JSONArray) firstObj.get(SAPServerConstants.RESULTS);
	
		return resultsArray;
	}

	public HttpGet getSAPGetRequest(String myurl) throws Exception {
		HttpGet request = new HttpGet(myurl);
		request.setHeader(ApplicationConstants.HEADER_CONTENT_TYPE, ApplicationConstants.APPLICATION_JSON);
		String auth = username + ApplicationConstants.DELIMITER_COLON + password;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
		String authHeader = ApplicationConstants.BASIC_AUTH + new String(encodedAuth);
		request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		return request;

	}
	
	@Override
	public PaginationDto<Employee> searchByField(String sDate, String eDate, String firstName, String lastName, String empId,
			String department, String designation, String employeeType, String cardNo, String lanyard, String status,
			int pageno, String sortField, String sortDir) {
		
		Date startDate = null;
		Date endDate = null;
		if (!sDate.isEmpty() && !eDate.isEmpty()) {
			SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
			try {
				startDate = format.parse(sDate);
				endDate = format.parse(eDate);
				
				endDate = calendarUtil.getConvertedDate(endDate, NumberConstants.TWENTY_THREE, NumberConstants.FIFTY_NINE, NumberConstants.FIFTY_NINE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Employee> page = getEmployeePage(startDate,endDate, firstName,lastName, empId, department, designation,employeeType,cardNo,lanyard,status, pageno, sortField,
				sortDir);
        List<Employee> employeeList =  page.getContent();
        List<Employee> employeeWithImgList = new ArrayList<Employee>();
        for (Employee employee : employeeList) {
			byte[] image = searchEmployeeImage(employee.getId());
			employee.setCropImage(image);
			employeeWithImgList.add(employee);
		}
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Employee> dtoList = new PaginationDto<Employee>(employeeWithImgList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}
	private byte[] searchEmployeeImage(long id) {
		Employee emp = new Employee();
		emp.setId(id);
		List<Image> imageList = imageRepository.findByEmployee(emp);
		byte[] bytes = null;
		if (null != imageList) {

			try {
				for (Image image : imageList) {
					String imagePath = image.getResizePath();
					InputStream inputStream = new FileInputStream(imagePath);
					bytes = IOUtils.toByteArray(inputStream);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bytes;
	}
	private Page<Employee> getEmployeePage(Date startDate,Date endDate, String firstName, String lastName, String empId, String department,
			String designation, String employeeType,String cardNo, String lanyard, String status, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		Specification<Employee> isDeletedFalse = generalSpecificationEmployee.isDeletedSpecification(false);
		Specification<Employee> dateSpec = generalSpecificationEmployee.dateSpecification(startDate, endDate, ApplicationConstants.LAST_MODIFIED_DATE);
		Specification<Employee> containsFirstName = generalSpecificationEmployee.stringSpecification(firstName,EmployeeConstants.FIRST_NAME);
		Specification<Employee> containsLastName = generalSpecificationEmployee.stringSpecification(lastName,EmployeeConstants.LAST_NAME);
		Specification<Employee> empIdSpc = generalSpecificationEmployee.stringSpecification(empId, EmployeeConstants.EMPLOYEE_ID);
		Specification<Employee> deptSpec = generalSpecificationEmployee.stringSpecification(department, EmployeeConstants.DEPARTMENT);
		Specification<Employee> designationSpc = generalSpecificationEmployee.stringSpecification(designation, EmployeeConstants.DESIGNATION);
		Specification<Employee> employeeTypSpc = generalSpecificationEmployee.foreignKeyStringSpecification(employeeType, EmployeeConstants.EMPLOYEE_TYPE,ApplicationConstants.NAME);
		Specification<Employee> cardNoSpec = generalSpecificationEmployee.stringSpecification(cardNo,EmployeeConstants.CARD_ID);
		Specification<Employee> lanyardSpec = generalSpecificationEmployee.stringSpecification(lanyard,EmployeeConstants.LANYARD);
		Specification<Employee> statusSpc = generalSpecificationEmployee.stringSpecification(status, EmployeeConstants.STATUS);
		
    	Page<Employee> page = employeeRepository.findAll(dateSpec.and(containsFirstName).and(containsLastName).and(cardNoSpec)
    			.and(empIdSpc).and(deptSpec).and(designationSpc).and(isDeletedFalse).and(employeeTypSpc).and(statusSpc).and(lanyardSpec),pageable);
		return page;
	}

	@SuppressWarnings("unchecked")
	public Page<Employee> searchByField(int pageNo, int pageSize, String sortField, String sortDirection,
			SearchRequestDto paginatedDto, Principal principal) {

		ObjectMapper oMapper = new ObjectMapper();

		Map<String, String> map = oMapper.convertValue(paginatedDto.getSearchData(), Map.class);

		Date startDate = null;
		Date endDate = null;

		if ((null != map.get("startDate")) && (null != map.get("endDate"))) {
			SimpleDateFormat format = new SimpleDateFormat(
					ApplicationConstants.DATE_TIME_FORMAT_OF_US_WITH_MILLISECOND);
			try {
				startDate = format.parse(map.get("startDate"));
				endDate = format.parse(map.get("endDate"));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (null == sortDirection || sortDirection.isEmpty()) {
			sortDirection = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		Specification<Employee> fieldSpc = dateSpecification(startDate, endDate, "lastModifiedDate");
		Page<Employee> allEmployee = employeeRepository.findAll(fieldSpc, pageable);

		return allEmployee;
	}

	

	public Specification<Employee> dateSpecification(Date startDate, Date endDate, String field) {
		return (root, query, cb) -> {
			if (null == startDate && null == endDate) {
				return cb.conjunction();
			}
			return cb.between(root.<Date>get(field), startDate, endDate);
		};
	}

	@Override
	public void saveEmployeeAccessLevelAssociation(Employee employee, Long id, Principal principal) {
		
		Employee employeeObj = getById(id);
		
		checkAccessLevelAndSendEmail(employee, employeeObj);
		
		employeeObj.setAccessLevel(employee.getAccessLevel());
		
		employeeRepository.save(employeeObj);
		
	}

	private void checkAccessLevelAndSendEmail(Employee employee, Employee employeeObj) {
		
		SimpleDateFormat sdf= new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		
		EmailSetup emailSetupAdd =  emailSetupRepository.findById(8l).get();
		if("Active".equalsIgnoreCase(emailSetupAdd.getStatus())){
			List<String> oldAccessLevelList = new ArrayList<>();
			
			for(AccessLevel acc: employeeObj.getAccessLevel()) 
				oldAccessLevelList.add(acc.getName());
			
			List<String> addList = new ArrayList<>();
			for(AccessLevel acc: employee.getAccessLevel()) {
				if(!oldAccessLevelList.contains(acc.getName())) 
					addList.add(acc.getName());
			}
			
			sendEmail(employeeObj, sdf, addList,emailSetupAdd, "Add");
			
		}
		
		EmailSetup emailSetupRemove =  emailSetupRepository.findById(9l).get();
		if("Active".equalsIgnoreCase(emailSetupRemove.getStatus())){
			List<String> newAccessLevelList = new ArrayList<>();
			
			for(AccessLevel acc: employee.getAccessLevel()) 
				newAccessLevelList.add(acc.getName());
			
			List<String> revokeList = new ArrayList<>();
			for(AccessLevel oldAcc: employee.getAccessLevel()) {
				if(!newAccessLevelList.contains(oldAcc.getName())) 
					revokeList.add(oldAcc.getName());
			}
			
			sendEmail(employeeObj, sdf, revokeList,emailSetupRemove, "Remove");
		}
		
	}

	private void sendEmail(Employee employee, SimpleDateFormat sdf, List<String> accessLevelList,EmailSetup emailSetup, String flag) {
		
		if("Add".equalsIgnoreCase(flag)){
			if(!accessLevelList.isEmpty()) {
				String acclevelName="";
				for(int i=0;i<accessLevelList.size();i++) {
					acclevelName+=(i+1)+". "+accessLevelList.get(i)+"\n";
				}
				String body = EmailSetupConstants.GIVING_ACCESS.formatted(emailSetup.getName(),employee.getEmployeeId(),
						employee.getFirstName()+" "+employee.getLastName(),sdf.format(new Date()),acclevelName);
				saveEmailLog(emailSetup, body);
			}
		}
		
		if("Remove".equalsIgnoreCase(flag)){
			if(!accessLevelList.isEmpty()) {
				String acclevelName="";
				for(int i=0;i<accessLevelList.size();i++) {
					acclevelName+=(i+1)+". "+accessLevelList.get(i)+"\n";
				}
				String body = EmailSetupConstants.REVOKE_ACCESS.formatted(emailSetup.getName(),employee.getEmployeeId(),
						employee.getFirstName()+" "+employee.getLastName(),sdf.format(new Date()),acclevelName);
				saveEmailLog(emailSetup, body);
			}
		}
	}

	private void saveEmailLog(EmailSetup emailSetup, String body) {
		try {
			emailSetupServiceImpl.sendEmail(emailSetup, body);
			
			EmailLogs emailLogs = new EmailLogs();
			emailLogs.setDate(new Date());
			emailLogs.setType(emailSetup.getSubject());
			emailLogs.setToEmailId(emailSetup.getTo());
			emailLogsRepository.save(emailLogs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void saveEmployeeMetalException(Employee employee, Long id, Principal principal) {
		Employee employeeObj = getById(id);
		employeeObj.setMetalExceptions(employee.getMetalExceptions());
		employeeRepository.save(employeeObj);
		
	}
	
//   @Scheduled(cron = "0 0 0/3 * * ?")
	public void syncEmployeeListFromBioSecurity() {
		try {
			
			String query = "select count(id) from att_person";
			ResultSet countStr = bioSecurityServerUtil.jdbcConnection(query);
			int count=0;
			if(null != countStr) {
				countStr.next();
				count= countStr.getInt(1);
			}
			
			int limit = 1000;
			int offSet = 0;
			int countEnroll=0;
			
			List<Employee> employees = new ArrayList<Employee>();
			while((offSet <= count) || ( countEnroll < count)) {
				query = "select * from att_person ORDER BY id OFFSET "+offSet+ " ROWS FETCH NEXT "+limit+" ROWS ONLY"; 
				ResultSet resultSet = bioSecurityServerUtil.jdbcConnection(query);
				if(null != resultSet) {
					while (resultSet.next()) {
						String empId = resultSet.getString("pers_person_pin");
						Employee employee =  employeeRepository.findByEmployeeId(empId);
						
						if(null == employee) {
							employee = new Employee();
						}
						employee.setEmployeeId(empId);
						employee.setLastName(resultSet.getString("pers_person_lastname"));
						employee.setFirstName(resultSet.getString("pers_person_name"));
						employee.setDepartment(resultSet.getString("auth_dept_name"));
						employee.setJoinDate(resultSet.getDate("hire_date"));
						
						employees.add(employee);
					}
				}
				employeeRepository.saveAll(employees);
				employees.clear();
				countEnroll += employees.size();
				offSet += limit;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void syncAllEmployeeCardNoFromBioSecurityDb() {
		try {
			Map<String, Employee> employeeMap = employeeObjectMap.getEmployeeByEmpId();
			Date currentDate= new Date();
		    List<Employee> employeeList=employeeRepository.findAllByIsDeletedFalse();
			List<Employee> employees = new ArrayList<Employee>();
			for(Employee employee:employeeList) {
				System.out.println(employee.getEmployeeId());
				String query = "select * from pers_card where person_pin="+employee.getEmployeeId(); 
				ResultSet resultSet = bioSecurityServerUtil.jdbcConnection(query);
				if(null != resultSet) {
					while (resultSet.next()) {
						String empId = resultSet.getString("person_pin");
						Employee emp =  employeeMap.get(empId);
						
						if(null != emp && (null!=resultSet.getString("card_no")||!resultSet.getString("card_no").isEmpty())) {
							
						employee.setCardId(resultSet.getString("card_no"));
						
						employees.add(employee);
						
					}
				}
			}
				if(employees.size()==1000) {
					employeeRepository.saveAll(employees);
					employees.clear();
				}
			}
			if(employees.size()>0) 
			  employeeRepository.saveAll(employees);
			LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate,"BS Employee Pull");
			lastSyncStatusRepository.save(lastSyncStatus);	

			} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	@Scheduled(cron = "0 0 0/5 * * ?")
	public void syncUpdatedEmployeeCardNoFromBioSecurityDb() {
		try {
			LastSyncStatus lastSyncStatus =lastSyncStatusRepository.findByActivity("BS Employee Pull");
			Date lastSyncDate=lastSyncStatus.getLastSyncTime();
			Map<String, Employee> employeeMap = employeeObjectMap.getEmployeeByEmpId();
			Date currentDate= new Date();
		    List<Employee> employeeList=employeeRepository.findByLastModifiedDateCustom(lastSyncDate,currentDate);
			List<Employee> employees = new ArrayList<Employee>();
			for(Employee employee:employeeList) {
				String query = "select * from pers_card where person_pin="+employee.getEmployeeId(); 
				ResultSet resultSet = bioSecurityServerUtil.jdbcConnection(query);
				if(null != resultSet) {
					while (resultSet.next()) {
						String empId = resultSet.getString("person_pin");
						Employee emp =  employeeMap.get(empId);
						
						if(null != emp) {
							
						employee.setCardId(resultSet.getString("card_no"));
						
						employees.add(employee);
					}
				}
			}
				if(employees.size()==1000) {
					employeeRepository.saveAll(employees);
					employees.clear();
				}
			}
			if(employees.size()>0) 
			  employeeRepository.saveAll(employees);
			lastSyncStatus = setLastSyncStatus(currentDate,"BS Employee Pull");
			lastSyncStatusRepository.save(lastSyncStatus);	

			} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String storeEmployeeAccessZoneList(MultipartFile file) {
		try {
			List<Employee> employeeList = excelEmployeeImport.parseExcelFileAccessLevel(file.getInputStream());
			employeeRepository.saveAll(employeeList);
			return "File uploaded successfully!";
		} catch (IOException e) {
			e.printStackTrace();
			return "Fail! -> uploaded filename: " + file.getOriginalFilename();
		}
	}
	
	public void pushHundredEmployeeToBiosecurity(){
		try {
				List<Employee> employeeList=employeeRepository.getHundredEmployee();
				if(0!=employeeList.size()) {
					for(Employee employee:employeeList) {
						bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
					}
				}
	
	}catch (Exception e) {
		e.printStackTrace();
	}
	}
	
	public void pushAllEmployeeToBiosecurity(){
		Date currentDate= new Date();
		try {
			
			int limit=NumberConstants.HUNDRED;
			int offset=0;
			while(true) {
				Pageable pageable = PageRequest.of(offset, limit,Sort.by("id").ascending());
				Page<Employee> employeePage=employeeRepository.getAllByLimit(pageable);
				List<Employee> employeeList=employeePage.getContent();
				if(0!=employeeList.size()) {
					for(Employee employee:employeeList) {
							 bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
					}
				}
				else
					break;
				
				offset++;
				
			}
			LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate,"BS Employee Push");
			lastSyncStatusRepository.save(lastSyncStatus);
	
	}catch (Exception e) {
		e.printStackTrace();
	}
	}
	
//	@Scheduled(cron = "0 0 0/6 * * ?")
	public void pushSFUpdatedEmployeeToBiosecurity(){
		LastSyncStatus lastSyncStatus =lastSyncStatusRepository.findByActivity("BS Employee Push");
		Date lastSyncDate=lastSyncStatus.getLastSyncTime();
		Date currentDate= new Date();
		try {
			
			int limit=NumberConstants.HUNDRED;
			int offset=0;
			
			while(true) {
				Pageable pageable = PageRequest.of(offset, limit,Sort.by("id").ascending());
				Page<Employee> employeePage=employeeRepository.findByLastModifiedDateCustom(lastSyncDate,currentDate,pageable);
				List<Employee> employeeList=employeePage.getContent();
				if(0!=employeeList.size()) {
					for(Employee employee:employeeList) {
						bioSecurityServerUtil.addEmployeeToBioSecurity(employee);
					}
				}
				else
					break;
				
				offset++;
				
			}
			 lastSyncStatus = setLastSyncStatus(currentDate,"BS Employee Push");
			lastSyncStatusRepository.save(lastSyncStatus);
	
	}catch (Exception e) {
		e.printStackTrace();
	}
	}
	
	public void pullHundredEmployeeFromBiosecurityAPI(){
		try {
		List<Employee> employeeList=employeeRepository.getHundredEmployee();
		
		for(Employee employee:employeeList) {
			if(null==employee.getCardId()) {
				JSONObject dataObject= bioSecurityServerUtil.getEmployeeFromBioSecurity(employee.getEmployeeId());
				if(null!=dataObject) {
					System.out.println(employee.getEmployeeId());
					employee.setCardId((String) dataObject.get("cardNo"));
					employeeRepository.save(employee);
					}
				}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void pullAllEmployeeFromBiosecurityAPI(){
		try {
			List<Employee> employeeList=employeeRepository.findAllByIsDeletedFalse();
			Date currentDate= new Date();
			
			for(Employee employee:employeeList) {
					JSONObject dataObject= bioSecurityServerUtil.getEmployeeFromBioSecurity(employee.getEmployeeId());
					if(null!=dataObject) {
						System.out.println(employee.getEmployeeId());
						String base64=(String) dataObject.get("personPhoto");
						if(null!=base64) 
							imageProcessingUtil.saveEmployeeImageFromBase64(base64, employee);
						
			}
			LastSyncStatus lastSyncStatus = setLastSyncStatus(currentDate,"BS Employee Pull");
			lastSyncStatusRepository.save(lastSyncStatus);
			}
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}
//	@Scheduled(cron = "0 0 0/24 * * ?")
	public void pullSFUpdatedEmployeeFromBiosecurityAPI(){
		try {
			LastSyncStatus lastSyncStatus =lastSyncStatusRepository.findByActivity("BS Employee Pull");
			Date lastSyncDate=lastSyncStatus.getLastSyncTime();
			Date currentDate= new Date();
		List<Employee> employeeList=employeeRepository.findByLastModifiedDateCustom(lastSyncDate,currentDate);
		
		for(Employee employee:employeeList) {
			byte[] bytes=searchEmployeeImage(employee.getId());
			if(null==bytes) {
				JSONObject dataObject= bioSecurityServerUtil.getEmployeeFromBioSecurity(employee.getEmployeeId());
				if(null!=dataObject) {
					System.out.println(employee.getEmployeeId());
					String base64=(String) dataObject.get("personPhoto");
					if(null!=base64) 
						imageProcessingUtil.saveEmployeeImageFromBase64(base64, employee);
				}
			}
	}
		
		lastSyncStatus=setLastSyncStatus(currentDate,"BS Employee Pull");
		lastSyncStatusRepository.save(lastSyncStatus);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public String storeEmployeeMasterList(MultipartFile file) {
		try {
			List<Employee> employeeList = excelEmployeeImport.parseExcelFileEmployeeMasterData(file.getInputStream());
			employeeRepository.saveAll(employeeList);
			
			return "File uploaded successfully!";
		} catch (IOException e) {
			e.printStackTrace();
			return "Fail! -> uploaded filename: " + file.getOriginalFilename();
		}
	
	}
	
	
}

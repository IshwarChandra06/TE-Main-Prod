package com.eikona.tech.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.dto.EmployeeDto;
import com.eikona.tech.dto.PaginatedDto;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.dto.SearchRequestDto;
import com.eikona.tech.entity.AccessLevel;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.MetalException;
import com.eikona.tech.export.ExportEmployeeMasterData;
import com.eikona.tech.export.ExportMetalException;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.EmployeeTypeRepository;
import com.eikona.tech.repository.LanyardTypeRepository;
import com.eikona.tech.service.AccessLevelService;
import com.eikona.tech.service.EmployeeService;
import com.eikona.tech.service.EmployeeTypeService;
import com.eikona.tech.service.MetalExceptionService;
import com.eikona.tech.service.impl.EmployeeServiceImpl;
import com.eikona.tech.util.ImageProcessingUtil;

@Controller
public class EmployeeController {
	
	@Autowired
	private EmployeeService employeeService;
	
	@Autowired
	private EmployeeTypeService employeeTypeService;
	
	@Autowired
	private EmployeeTypeRepository employeeTypeRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private LanyardTypeRepository lanyardTypeRepository;
	
	@Autowired
	private AccessLevelService accessLevelService;
	
	@Autowired
	private EmployeeServiceImpl employeeServiceImpl;
	
	@Autowired
	private ExportEmployeeMasterData exportEmployee;
	
	@Autowired
	private MetalExceptionService metalExceptionService;
	
	@Autowired
	private ExportMetalException exportMetalException;
	
	@Autowired
	private ImageProcessingUtil imageProcessingUtil;
	
	@GetMapping(value="/employee")
	@PreAuthorize("hasAuthority('employee_view')")
	public String employeeList(Model model) {
		model.addAttribute("listLanyard", lanyardTypeRepository.findAllNameCustom());
		model.addAttribute("listEmployeeType", employeeTypeRepository.findAllNameCustom());
		return "employee/employee_list";
	}
	
	@GetMapping("/employee/new")
	@PreAuthorize("hasAuthority('employee_create')")
	public String newEmployee(Model model) {
		model.addAttribute("listEmployeeType", employeeTypeRepository.findContractorAndVisitorCustom());
		Employee employee = new Employee();
		model.addAttribute("employee", employee);
		model.addAttribute("title", "New Contractor");
		return "employee/employee_new";
	}
	@PostMapping("/employee/add")
	@PreAuthorize("hasAnyAuthority('employee_create','employee_update')")
	public String saveEmployee(@RequestParam("files") MultipartFile file, @ModelAttribute("employee") Employee employee,
			Model model, @Valid Employee emp, Errors errors, String title,BindingResult bindingResult) {
		if (errors.hasErrors()) {
			model.addAttribute("listEmployeeType", employeeTypeService.getAll());
			model.addAttribute("title", title);
			return "employee/employee_new";
		} else {
				try {
					SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
					if(!employee.getJoinDateStr().isEmpty())
					employee.setJoinDate(dateFormat.parse(employee.getJoinDateStr()));
					if(!employee.getEndDateStr().isEmpty())
						employee.setEndDate(dateFormat.parse(employee.getEndDateStr()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Employee findEmployee= employeeRepository.findByEmployeeIdAndIsDeletedFalse(employee.getEmployeeId());
			if (null == employee.getId()) {
				
				if(null!=findEmployee){
					model.addAttribute("title", title);
					model.addAttribute("listEmployeeType", employeeTypeService.getAll());
				    bindingResult.rejectValue("employeeId", "error.employeeId", "Employee id is already exist!");
				    return "employee/employee_new";
				}
				employee.setStatus("Active");
				employee=employeeService.save(employee);
				if (null != file && !file.getOriginalFilename().isEmpty()) {
					imageProcessingUtil.saveEmployeeImageWhileEnrolling(file,employee);
				}

			} else {
				Employee employeeObj = employeeService.getById(employee.getId());
				if(null!=findEmployee && !(employeeObj.getEmployeeId().equalsIgnoreCase(employee.getEmployeeId()))){
					model.addAttribute("title", title);
					model.addAttribute("listEmployeeType", employeeTypeService.getAll());
				    bindingResult.rejectValue("employeeId", "error.employeeId", "Employee id is already exist!");
				    return "employee/employee_new";
				}
				
				employee.setCreatedBy(employeeObj.getCreatedBy());
				employee.setCreatedDate(employeeObj.getCreatedDate());
				employee.setJoinDate(employeeObj.getJoinDate());
				employee.setEndDate(employeeObj.getEndDate());
				employee.setStartDate(employeeObj.getStartDate());
				employee.setCardIssueDate(employeeObj.getCardIssueDate());
				employee.setStatus(employeeObj.getStatus());
				employee.setSource(employeeObj.getSource());
				employee.setRelUserId(employeeObj.getRelUserId());
				employee.setLanyardColor(employeeObj.getLanyardColor());
				employee.setAccessLevel(employeeObj.getAccessLevel());
				employee.setMetalExceptions(employeeObj.getMetalExceptions());
				employee=employeeService.save(employee);
				if (null != file && !file.getOriginalFilename().isEmpty()) {
					imageProcessingUtil.saveEmployeeImageWhileEnrolling(file,employee);
				}
				
			}

			return "redirect:/employee";
		}
	}
	@GetMapping("/employee/edit/{id}")
	@PreAuthorize("hasAuthority('employee_update')")
	public String editEmployee(@PathVariable(value = "id") long id, Model model) {

		model.addAttribute("listEmployeeType", employeeTypeService.getAll());

		Employee employee = employeeService.getById(id);
		model.addAttribute("employee", employee);
		model.addAttribute("title", "Update Employee");
		return "employee/employee_new";
	}

	@GetMapping("/employee/delete/{id}")
	@PreAuthorize("hasAuthority('employee_delete')")
	public String deleteEmployee(@PathVariable(value = "id") long id, Principal principal) {
		this.employeeService.deleteById(id, principal);
		return "redirect:/employee";
	}
	@GetMapping("/employee/view/{empId}")
	@PreAuthorize("hasAuthority('employee_view')")
	public String viewEmployee(@PathVariable(value = "empId") String empId, Model model) {
		Employee employee = employeeRepository.findByEmployeeId(empId);
		model.addAttribute("employee", employee);
		model.addAttribute("id", employee.getId());
		model.addAttribute("empId", empId);
		model.addAttribute("title", "View Employee");
		return "employee/employee_view";
	}
	
	@GetMapping("/employee-to-accesslevel/association/{id}")
	@PreAuthorize("hasAuthority('employee_view')")
	public String employeeAccessLevelAssociation(@PathVariable(value = "id") long id, Model model) {

		List<AccessLevel> accessLevelList = accessLevelService.getAll();
		model.addAttribute("listAccesslevel", accessLevelList);
		Employee employee = employeeService.getById(id);
		model.addAttribute("employee", employee);
		model.addAttribute("id", id);
		model.addAttribute("redirect", "/employee/view/"+employee.getEmployeeId());
		return "employee/employee_accesslevel";
	}
	
	@GetMapping("/employee-metal-exception/{id}")
	@PreAuthorize("hasAuthority('employee_view')")
	public String employeeMetalException(@PathVariable(value = "id") long id, Model model) {

		List<MetalException> metalExceptionList = metalExceptionService.getAll();
		model.addAttribute("listMetalException", metalExceptionList);
		Employee employee = employeeService.getById(id);
		model.addAttribute("employee", employee);
		model.addAttribute("redirect", "/employee/view/"+employee.getEmployeeId());
		model.addAttribute("id", id);
		return "employee/employee_metal_exception";
	}
	
	@GetMapping("/import/employee-accesslevel-list")
	@PreAuthorize("hasAuthority('employee_import')")
	public String importEmployeeList() {
		return "multipartfile/uploadAccessZone";
	}

	@PostMapping("/upload/employee-accesslevel-list/excel")
	@PreAuthorize("hasAuthority('employee_import')")
	public String uploadEmployeeAccessZone(@RequestParam("uploadfile") MultipartFile file, Model model) {
		String message = employeeService.storeEmployeeAccessZoneList(file);
		model.addAttribute("message", message);
		return "multipartfile/uploadAccessZone";
	}
	
	@GetMapping("/import/employee-master-list")
	@PreAuthorize("hasAuthority('employee_import')")
	public String importEmployeeMasterData() {
		return "multipartfile/uploadEmployeeData";
	}

	@PostMapping("/upload/employee-master-list/excel")
	@PreAuthorize("hasAuthority('employee_import')")
	public String uploadEmployeeList(@RequestParam("uploadfile") MultipartFile file, Model model) {
		String message = employeeService.storeEmployeeMasterList(file);
		model.addAttribute("message", message);
		return "multipartfile/uploadEmployeeData";
	}

	@PostMapping("/employee-to-accesslevel/association/save")
	@PreAuthorize("hasAuthority('employee_create')")
	public String saveEmployeeAccessLevelAssociation(@ModelAttribute("employee") Employee employee, Long id,
			Principal principal) {
		employeeService.saveEmployeeAccessLevelAssociation(employee, id, principal);
		return "redirect:/employee";

	}
	
	@PostMapping("/employee-metal-exception/save")
	@PreAuthorize("hasAuthority('employee_create')")
	public String saveEmployeeMetalException(@ModelAttribute("employee") Employee employee, Long id,Principal principal) {
		
		employeeService.saveEmployeeMetalException(employee, id, principal);
		return "redirect:/employee";

	}
	@RequestMapping(value = "/employee/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_view')")
	public @ResponseBody PaginationDto<Employee> searchEmployee(String sDate,String eDate, String firstName, String lastName,String empId,String department,String designation,
			String employeeType,String cardNo,String lanyard,String status, int pageno, String sortField, String sortDir) {
		
		PaginationDto<Employee> dtoList = employeeService.searchByField(sDate,eDate,firstName,lastName,empId,department,designation,employeeType.trim(),cardNo,lanyard.trim(),status,pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value = "/sync/employee-from-sf", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_view')")
	public @ResponseBody void syncEmployeeFromSF() {
		employeeServiceImpl.syncEmployeeListFromSap();
	}
	
	@RequestMapping(value = "/edit/employee-from-sf", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_view')")
	public @ResponseBody void updateEmployeeFromSF() {
		employeeServiceImpl.updateEmployeeListFromSapByDateTime();
	}
	
//	@RequestMapping(value = "/sync/employee-from-bs", method = RequestMethod.GET)
//	@PreAuthorize("hasAuthority('employee_create')")
//	public @ResponseBody void syncEmployeeFromBS() {
//		employeeServiceImpl.syncEmployeeListFromBioSecurity();
//	}
	
	@RequestMapping(value = "/sync/employee-to-bs", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_view')")
	public @ResponseBody void syncEmployeeToBS() {
		employeeServiceImpl.pushAllEmployeeToBiosecurity();
	}
	
	@RequestMapping(value = "/sync/hundred-employee-to-bs", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_view')")
	public @ResponseBody void syncHundredEmployeeToBS() {
		employeeServiceImpl.pushHundredEmployeeToBiosecurity();
	}
	
	@RequestMapping(value = "/sync/employee-from-bs", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_view')")
	public @ResponseBody void syncEmployeeFromBS() {
		employeeServiceImpl.pullAllEmployeeFromBiosecurityAPI();
	}
	
	@RequestMapping(value = "/sync/hundred-employee-from-bs", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_view')")
	public @ResponseBody void syncHundredEmployeeFromBS() {
		employeeServiceImpl.pullHundredEmployeeFromBiosecurityAPI();
	}
	
	@RequestMapping(value = "/api/employee/search", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('employee_view')")
	public @ResponseBody PaginatedDto<EmployeeDto> search(@RequestBody SearchRequestDto paginatedDto,
			Principal principal) {

		String message = "";
		String messageType = "";
		PaginatedDto<EmployeeDto> paginatedDtoList = null;
		List<EmployeeDto> employeeDtoList = new ArrayList<>();
		try {
			
			Page<Employee> page = employeeService.searchByField(paginatedDto.getPageNo(),
					paginatedDto.getPageSize(), paginatedDto.getSortField(), paginatedDto.getSortOrder(), paginatedDto,
					principal);
			List<Employee> employeeList = page.getContent();

			for (Employee employee : employeeList) {
				EmployeeDto employeeDto = new EmployeeDto();
				employeeDto.setId(employee.getId());
				employeeDto.setLastModifiedDate(employee.getLastModifiedDate());
				employeeDto.setFirstName(employee.getFirstName());
				employeeDto.setLastName(employee.getLastName());
				employeeDto.setEmployeeId(employee.getEmployeeId());
				employeeDto.setDepartment(employee.getDepartment());
				employeeDto.setFunction(employee.getDesignation());
				employeeDto.setJoinDate(employee.getJoinDate());
				employeeDto.setEndDate(employee.getEndDate());
				employeeDto.setCadre(employee.getCadre());
				employeeDto.setPayGrade(employee.getPayGrade());
				employeeDto.setManagerId(employee.getManagerId());
				employeeDto.setManagerName(employee.getManagerName());
				employeeDto.setCardId(employee.getEmployeeId());
				employeeDto.setCardIssueDate(null);
				employeeDto.setBuilding(ApplicationConstants.DELIMITER_EMPTY);
				employeeDto.setAccessLevel(employee.getAccesslevels());
				employeeDto.setUserState(employee.getStatus());
				employeeDto.setLanyardColor(employee.getLanyardColor());
				

				employeeDtoList.add(employeeDto);

			}

			List<Employee> totalEmployeeList = employeeService.getAll();
			Page<Employee> totalPage = new PageImpl<Employee>(totalEmployeeList);
			message = "Success";
			messageType = "S";
			paginatedDtoList = new PaginatedDto<EmployeeDto>(employeeDtoList, page.getTotalPages(),
					page.getNumber() + 1, page.getSize(), page.getTotalElements(), totalPage.getTotalElements(),
					message, messageType);

		} catch (Exception e) {
			e.printStackTrace();
			return new PaginatedDto<EmployeeDto>(employeeDtoList, 0, 0, 0, 0, 0, "Failed", "E");
		}
		return paginatedDtoList;
	}
	
	@RequestMapping(value="/employee/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_export')")
	public void exportToFile(HttpServletResponse response,String sDate,String eDate, String firstName, String lastName,String empId,String department,
			String designation,String employeeType,String cardNo,String lanyard,String status,String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportEmployee.fileExportBySearchValue(response,sDate, eDate, firstName,lastName,empId,department, designation,employeeType.trim(),cardNo,lanyard.trim(),status,flag );
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value="/metal-exception/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('employee_export')")
	public void metalExceptionReportExportToFile(HttpServletResponse response,String sDate,String eDate,  String firstName, String lastName,String empId,String department,
			String designation,String employeeType,String cardNo,String lanyard,String status,String flag) throws FileNotFoundException {
		response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportMetalException.metalExceptionExportBySearchValue(response,sDate, eDate, firstName,lastName,empId,department, designation,employeeType.trim(),cardNo,lanyard.trim(),status,flag );
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping("/employee-excel-template-download")
	@PreAuthorize("hasAuthority('employee_import')")
	public void downloadEmployeeListExcelTemplate(HttpServletResponse response) throws IOException {
        String filename = "excel/Employee_import_template.xlsx";
        try {
        	
        	String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee_import_template.xlsx";
			response.setHeader(headerKey, headerValue);
			FileInputStream inputStream = new FileInputStream(new File(filename));
			Workbook workBook = new XSSFWorkbook(inputStream);
			FileOutputStream fileOut = new FileOutputStream(filename);
			workBook.write(fileOut);
			ServletOutputStream outputStream = response.getOutputStream();
			workBook.write(outputStream);
			workBook.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping("/employee-accesslevel-excel-template-download")
	@PreAuthorize("hasAuthority('employee_import')")
	public void downloadEmployeeAccessLevelExcelTemplate(HttpServletResponse response) throws IOException {
        String filename = "excel/Accesslevel_import_template.xlsx";
        try {
        	
        	String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Accesslevel_import_template.xlsx";
			response.setHeader(headerKey, headerValue);
			FileInputStream inputStream = new FileInputStream(new File(filename));
			Workbook workBook = new XSSFWorkbook(inputStream);
			FileOutputStream fileOut = new FileOutputStream(filename);
			workBook.write(fileOut);
			ServletOutputStream outputStream = response.getOutputStream();
			workBook.write(outputStream);
			workBook.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}

package com.eikona.tech.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Device;
import com.eikona.tech.export.ExportDevice;
import com.eikona.tech.repository.DeviceRepository;
import com.eikona.tech.service.AccessLevelService;
import com.eikona.tech.service.DeviceService;
import com.eikona.tech.service.impl.DeviceServiceImpl;

@Controller
public class DeviceController {

	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private AccessLevelService accessLevelService;
	
	@Autowired
	private DeviceServiceImpl deviceServiceImpl;
	
	@Autowired
	private ExportDevice exportDevice;

	@GetMapping("/device")
	@PreAuthorize("hasAuthority('device_view')")
	public String deviceList() {
		return "device/device_list";
	}
	
	@GetMapping("/device/delete/{id}")
	@PreAuthorize("hasAuthority('device_delete')")
	public String deleteDevice(@PathVariable(value = "id") long id) {
		this.deviceService.deleteById(id);
		return "redirect:/device";
	}
	@PostMapping("/device/add")
	@PreAuthorize("hasAuthority('device_update')")
	public String saveArea(@ModelAttribute("device") Device device, @Valid Device dev, Errors errors, String title,BindingResult bindingResult,
			Model model) {
		if (errors.hasErrors()) {
			model.addAttribute("listAccessLevel", accessLevelService.getAll());
			model.addAttribute("title", title);
			return "device/device_edit";
		} else {
			Device findDevice= deviceRepository.findBySerialNoAndIsDeletedFalse(device.getSerialNo());
			if (null == device.getId()) {
				if(null!=findDevice){
					model.addAttribute("title", title);
					model.addAttribute("listAccessLevel", accessLevelService.getAll());
				    bindingResult.rejectValue("serialNo", "error.serialNo", "Serial No is already exist!");
				    return "device/device_edit";
				}
				deviceService.save(device);
			}
				
			else {
				Device deviceObj = deviceService.getById(device.getId());
				if(null!=findDevice && !(deviceObj.getSerialNo().equalsIgnoreCase(device.getSerialNo()))){
					model.addAttribute("title", title);
					model.addAttribute("listAccessLevel", accessLevelService.getAll());
				    bindingResult.rejectValue("serialNo", "error.serialNo", "Serial No is already exist!");
				    return "device/device_edit";
				}
				device.setCreatedBy(deviceObj.getCreatedBy());
				device.setCreatedDate(deviceObj.getCreatedDate());
				device.setLastOnline(deviceObj.getLastOnline());
				deviceService.save(device);
			}
			return "redirect:/device";
		}

	}
	@GetMapping("/device/edit/{id}")
	@PreAuthorize("hasAuthority('device_update')")
	public String updateDevice(@PathVariable(value = "id") long id, Model model) {
		
		Device device = deviceService.getById(id);
		model.addAttribute("listAccessLevel", accessLevelService.getAll());
		model.addAttribute("device", device);
		model.addAttribute("title", "Update Device");
		return "device/device_edit";
	}
	@GetMapping("/sync/device")
	@PreAuthorize("hasAuthority('device_view')")
	public @ResponseBody void syncAccessLevel() {
		try {
			deviceServiceImpl.syncAndSaveDoor();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/device/search", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('device_view')")
	public @ResponseBody PaginationDto<Device> searchDoor(String name,String zone,String building,String plant,String accesslevel,String serialNo,String ipAddress,
			String status,int pageno, String sortField, String sortDir) {
		
		PaginationDto<Device> dtoList = deviceService.searchByField(name,zone,building,plant,accesslevel,serialNo,ipAddress,status, pageno, sortField, sortDir);
		return dtoList;
	}
	
	@RequestMapping(value="/device/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('device_export')")
	public void exportToFile(HttpServletResponse response,String name,String zone,String building,String plant,String accesslevel,String serialNo,
			String ipAddress,String status,String flag) {
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Device_" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportDevice.fileExportBySearchValue(response,name,zone,building,plant,accesslevel,serialNo,ipAddress,status,flag );
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}

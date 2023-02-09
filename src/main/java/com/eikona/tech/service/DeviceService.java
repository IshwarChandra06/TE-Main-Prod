package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Device;

public interface DeviceService {

	void deleteById(long id);

	PaginationDto<Device> searchByField(String name, String zone, String building, String plant, String accesslevel,
			String serialNo, String ipAddress, String status, int pageno, String sortField, String sortDir);

	Device getById(long id);

	void save(Device device);

}

package com.eikona.tech.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.entity.Device;
import com.eikona.tech.entity.DeviceHealthStatus;
import com.eikona.tech.repository.DeviceHealthStatusRepository;
import com.eikona.tech.repository.DeviceRepository;

@Component
@EnableScheduling
public class PingDeviceIpUtil {
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private DeviceHealthStatusRepository deviceHealthStatusRepository;

	@Scheduled(cron = "0 0/5 * * * ?")
	public void checkDevicePingStatus() throws UnknownHostException {
		SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US);
		try {
			List<Device> deviceList = deviceRepository.findAllByIsDeletedFalseWithIPAddressNotNullCustom();

			for (Device device : deviceList) {
				DeviceHealthStatus deviceHealthStatus = new DeviceHealthStatus();
				String dateStr = format.format(new Date());

				InetAddress geek = InetAddress.getByName(device.getIpAddress());

				if (geek.isReachable(10000)) {
					System.out.println(device.getName() + " is reachable");

					device.setLastOnline(format.parse(dateStr));
					device.setStatus("Active");

					deviceHealthStatus.setDevice(device);
					deviceHealthStatus.setDate(format.parse(dateStr));
					deviceHealthStatus.setTimeStr(dateStr);
					deviceHealthStatus.setStatus(1);

				} else {
					System.out.println("Sorry ! We can't reach to " + device.getName());
					device.setStatus("Inactive");

					deviceHealthStatus.setDevice(device);
					deviceHealthStatus.setDate(format.parse(dateStr));
					deviceHealthStatus.setTimeStr(dateStr);
					deviceHealthStatus.setStatus(0);

				}

				deviceRepository.save(device);
				deviceHealthStatusRepository.save(deviceHealthStatus);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

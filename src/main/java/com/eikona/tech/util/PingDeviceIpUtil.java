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
import com.eikona.tech.repository.DeviceRepository;

@Component
@EnableScheduling
public class PingDeviceIpUtil {
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Scheduled(cron = "0 0/13 * * * ?")
	public void checkDevicePingStatus() throws UnknownHostException{
		SimpleDateFormat format = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US);
		try {
			List<Device> deviceList=deviceRepository.findAllByIsDeletedFalse();
			
			for(Device device:deviceList) {
			  InetAddress geek = InetAddress.getByName(device.getIpAddress());
			  if (geek.isReachable(5000)) {
				  System.out.println(device.getName()+" is reachable");
				  String dateStr=format.format(new Date());
				  device.setLastOnline(format.parse(dateStr));
				  device.setStatus("Active");
			  }
			  else {
				  System.out.println("Sorry ! We can't reach to "+device.getName());
				  device.setStatus("Inactive");
			  }
				  
			  
			  deviceRepository.save(device);
			    
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		}
}

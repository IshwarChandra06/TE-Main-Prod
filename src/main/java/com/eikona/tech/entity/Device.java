package com.eikona.tech.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

@Entity(name = "te_device")
public class Device extends Auditable<String> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "door_id")
	private String doorId;
	
	@Column(name = "device_id")
	private String deviceId;
	
	@Column(unique = true,name = "serial_no")
	private String serialNo;
	
	@Column(name = "last_online")
	private Date lastOnline;
	
	@Column(name = "ip_address")
	private String ipAddress;
	
	@Column(name = "status")
	private String status;
	
	@ManyToOne
	@JoinColumn(name = "access_level_id")
	private AccessLevel accessLevel;
	
	@Column(name = "is_deleted")
	private boolean isDeleted;
	
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDoorId() {
		return doorId;
	}

	public void setDoorId(String doorId) {
		this.doorId = doorId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public Date getLastOnline() {
		return lastOnline;
	}

	public void setLastOnline(Date lastOnline) {
		this.lastOnline = lastOnline;
	}

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getStatus() {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateStr = format.format(new Date());
			Date date = format.parse(dateStr);
			Date lastonline = getLastOnline();
			
			long status = date.getTime() - lastonline.getTime();
			
			if(status<900000) {
				return "Green";
			}else if(status<3600000) {
				return "Amber";
			}else {
				return "Red";
			}
		}catch (Exception e) {
			e.printStackTrace();
			return "Red";
		}
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
	
	
	
}

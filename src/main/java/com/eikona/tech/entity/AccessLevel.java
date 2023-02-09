package com.eikona.tech.entity;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;


@Entity(name = "te_access_level")
public class AccessLevel extends Auditable<String> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "access_id")
	private String accessId;
	
	@Column(name = "name")
	private String name;
	
	@ManyToOne
	@JoinColumn(name="building_id")
	private Building building;
	
	@ManyToOne
	@JoinColumn(name="zone_id")
	private Zone zone;
	
	@ManyToMany
	private List<Device> device;
	
	private String devices;

	public Long getId() {
		return id;
	}

	public String getAccessId() {
		return accessId;
	}

	public String getName() {
		return name;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public List<Device> getDevice() {
		return device;
	}

	public void setDevice(List<Device> device) {
		this.device = device;
	}

	public String getDevices() {
		List<Device> doorList=getDevice();
		StringJoiner strJoiner= new StringJoiner(",");
		for(Device door:doorList) {
			strJoiner.add(door.getName());
		}
		return strJoiner.toString();
	}

	public void setDevices(String devices) {
		this.devices = devices;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}
	
	
}

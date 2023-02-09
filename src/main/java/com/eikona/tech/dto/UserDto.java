package com.eikona.tech.dto;

import com.eikona.tech.entity.Role;

public class UserDto {

	private Long id;

	private String userName;

	private String password;

	private boolean active;

	private Role role;

	
	private String phone;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	
	public UserDto() {
		super();
	}

	public UserDto(Long id, String userName, boolean active, Role role, String phone) {
		super();
		this.id = id;
		this.userName = userName;
		this.active = active;
		this.role = role;
		this.phone = phone;
	}
}
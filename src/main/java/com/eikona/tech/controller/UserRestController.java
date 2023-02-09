package com.eikona.tech.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.eikona.tech.entity.User;
import com.eikona.tech.service.UserService;


@RestController
@RequestMapping("/api/user")
public class UserRestController {

	@Autowired
	private UserService userService;
	

	@Autowired
	private PasswordEncoder passwordEncoder;
	
    
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public ResponseEntity<Object> list(Model model) {
		List<User> orgList = userService.getAll();
		return new ResponseEntity<>(orgList, HttpStatus.OK);
	}
 
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('user_create')")
	public ResponseEntity<Object> create(@RequestBody User user, Principal principal) {
		
		try {
			
			if (null == user.getUserName() || user.getUserName().isEmpty() || user.getPassword().isEmpty()
					|| null == user.getPassword()) {
				String message = "Username or password should not be empty.";
				return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
			}
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User createdUser = userService.save(user);
			
			return new ResponseEntity<>(createdUser, HttpStatus.OK);
		} catch (Exception e) {
			String message = "Contact Admin!!";
			return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('user_view')")
	public User get(@PathVariable long id) {
		return userService.getById(id);
	}

	@RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('user_update')")
	public ResponseEntity<Object> update(@PathVariable long id, @RequestBody User user) {
		try {
			
			if (null == user.getId()) {
				return new ResponseEntity<>("Id should not be empty", HttpStatus.BAD_REQUEST);
			}
			User userAudit = userService.getById(id);
			user.setCreatedBy(userAudit.getCreatedBy());
			user.setCreatedDate(userAudit.getCreatedDate());
			user.setPassword(userAudit.getPassword());
			User updatedUser = userService.save(user);
			return new ResponseEntity<>(updatedUser, HttpStatus.OK);
		} catch (Exception e) {
			String message = "Contact Admin!!";
			return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('user_delete')")
	public ResponseEntity<Object> delete(@PathVariable(value = "id") long id) {
		try {
			userService.deleteById(id);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			String message = "Contact Admin!!";
			return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
		}
	}
	
	
}

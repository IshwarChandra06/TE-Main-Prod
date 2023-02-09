package com.eikona.tech.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.eikona.tech.entity.User;
import com.eikona.tech.repository.UserRepository;
import com.eikona.tech.security.MyUserDetails;
@Service
public class MyUserDetailsService implements UserDetailsService {
	 @Autowired
	 private UserRepository userRepository;
	 
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Optional<User> user =userRepository.findByUserName(username);
    	user.orElseThrow(() -> new UsernameNotFoundException("Not Found: "+username));
    	
    	return user.map(MyUserDetails::new).get();
	}
	

}

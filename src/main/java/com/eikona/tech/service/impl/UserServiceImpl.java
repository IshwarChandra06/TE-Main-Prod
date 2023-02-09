package com.eikona.tech.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.constants.UserConstants;
import com.eikona.tech.util.GeneralSpecificationUtil;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.User;
import com.eikona.tech.repository.UserRepository;
import com.eikona.tech.service.UserService;

@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class UserServiceImpl implements UserService {

	@Autowired
	protected UserRepository userRepository;
	
	@Autowired
	private GeneralSpecificationUtil<User> generalSpecification;
	
	@Autowired
	private PasswordEncoder passwordEncoder;


	@Override
	public User getById(long id) {
		User result = null;

		Optional<User> catalog = userRepository.findById(id);

		if (catalog.isPresent()) {
			result = catalog.get();
		}

		return result;
	}

	@Override
	public List<User> getAll() {
		return userRepository.findAllByIsDeletedFalse();
	}

	@Override
	public User save(User entity) {
		entity.setDeleted(false);
		entity.setPassword(passwordEncoder.encode(entity.getPassword()));
		entity.setActive(true);
		return userRepository.save(entity);
	}

	@Override
	public void deleteById(long id) {
		User result = null;

		Optional<User> catalog = userRepository.findById(id);

		if (catalog.isPresent()) {
			result = catalog.get();
			result.setDeleted(true);
		}
		this.userRepository.save(result);
	}

	@Override
	public PaginationDto<User> searchByField(Long id, String name, String phone, String role, int pageno,
			String sortField, String sortDir) {

		if (null == sortDir || sortDir.isEmpty()) {
			sortDir = ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}

		Page<User> page = getSpecificationOfUser(id, name, phone, role, pageno, sortField, sortDir);
		
		List<User> userList = page.getContent();

		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir)) ? ApplicationConstants.DESC : ApplicationConstants.ASC;
		PaginationDto<User> dtoList = new PaginationDto<User>(userList, page.getTotalPages(), page.getNumber() + NumberConstants.ONE,
				page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<User> getSpecificationOfUser(Long id, String name, String phone, String role, int pageno, String sortField, String sortDir) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();
		
		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<User> idSpec = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<User> nameSpec = generalSpecification.stringSpecification(name, UserConstants.USER_NAME);
		Specification<User> phoneSpec = generalSpecification.stringSpecification(phone, UserConstants.PHONE);
		Specification<User> roleSpec = generalSpecification.foreignKeyStringSpecification(role, UserConstants.ROLE, ApplicationConstants.NAME);
		Specification<User> isDeletedFalse = generalSpecification.isDeletedSpecification(false);

		Page<User> page = userRepository.findAll(idSpec.and(nameSpec).and(isDeletedFalse).and(phoneSpec).and(roleSpec),
				pageable);
		return page;
	}

	
	
}

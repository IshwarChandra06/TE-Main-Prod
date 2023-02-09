package com.eikona.tech.service.impl;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.EmployeeType;
import com.eikona.tech.repository.EmployeeTypeRepository;
import com.eikona.tech.service.EmployeeTypeService;
import com.eikona.tech.util.GeneralSpecificationUtil;


@Service
public class EmployeeTypeServiceImpl implements EmployeeTypeService {

	@Autowired
	private EmployeeTypeRepository employeeTypeRepository;
	
	@Autowired
	private GeneralSpecificationUtil<EmployeeType> generalSpecification;

	@Override
	public List<EmployeeType> getAll() {
		return employeeTypeRepository.findAllByIsDeletedFalse();
	}

	@Override
	public void save(EmployeeType employeeType) {
		employeeType.setDeleted(false);
		this.employeeTypeRepository.save(employeeType);
	}

	@Override
	public EmployeeType getById(long id) {
		Optional<EmployeeType> optional = employeeTypeRepository.findById(id);
		EmployeeType employeeType = null;
		if (optional.isPresent()) {
			employeeType = optional.get();
		} else {
			throw new RuntimeException("Employee type not found for id "+ id);
		}
		return employeeType;
	}

	@Override
	public void deleteById(long id) {
		Optional<EmployeeType> optional = employeeTypeRepository.findById(id);
		EmployeeType employeeType = null;
		if (optional.isPresent()) {
			employeeType = optional.get();
			employeeType.setDeleted(true);
		} else {
			throw new RuntimeException("Employee type not found for id "+ id);
		}
		this.employeeTypeRepository.save(employeeType);
	}

	@Override
	public PaginationDto<EmployeeType> searchByField(Long id, String name, int pageno, String sortField,
			String sortDir) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<EmployeeType> page = getEmployeeTypePage(id, name, pageno, sortField, sortDir);
        List<EmployeeType> employeeTypeList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<EmployeeType> dtoList = new PaginationDto<EmployeeType>(employeeTypeList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<EmployeeType> getEmployeeTypePage(Long id, String name, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<EmployeeType> idSpc = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<EmployeeType> nameSpc = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<EmployeeType> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		
		
    	Page<EmployeeType> page = employeeTypeRepository.findAll(idSpc.and(nameSpc).and(isDeletedFalse),pageable);
		return page;
	}
	
	
	
	 

}

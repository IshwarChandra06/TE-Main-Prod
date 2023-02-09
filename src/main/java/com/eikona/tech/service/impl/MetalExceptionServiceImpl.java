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
import com.eikona.tech.entity.MetalException;
import com.eikona.tech.repository.MetalExceptionRepository;
import com.eikona.tech.service.MetalExceptionService;
import com.eikona.tech.util.GeneralSpecificationUtil;


@Service
public class MetalExceptionServiceImpl implements MetalExceptionService {

	@Autowired
	private MetalExceptionRepository metalExceptionRepository;
	
	@Autowired
	private GeneralSpecificationUtil<MetalException> generalSpecification;

	@Override
	public List<MetalException> getAll() {
		return metalExceptionRepository.findAllByIsDeletedFalse();
	}

	@Override
	public void save(MetalException metalException) {
		metalException.setDeleted(false);
		this.metalExceptionRepository.save(metalException);
	}

	@Override
	public MetalException getById(long id) {
		Optional<MetalException> optional = metalExceptionRepository.findById(id);
		MetalException metalException = null;
		if (optional.isPresent()) {
			metalException = optional.get();
		} else {
			throw new RuntimeException("Metal Exception not found for id "+ id);
		}
		return metalException;
	}

	@Override
	public void deleteById(long id) {
		Optional<MetalException> optional = metalExceptionRepository.findById(id);
		MetalException metalException = null;
		if (optional.isPresent()) {
			metalException = optional.get();
			metalException.setDeleted(true);
		} else {
			throw new RuntimeException("Metal Exception not found for id "+ id);
		}
		this.metalExceptionRepository.save(metalException);
	}

	@Override
	public PaginationDto<MetalException> searchByField(Long id, String name, int pageno, String sortField,
			String sortDir) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<MetalException> page = getMetalExceptionPage(id, name, pageno, sortField, sortDir);
        List<MetalException> metalExceptionList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<MetalException> dtoList = new PaginationDto<MetalException>(metalExceptionList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<MetalException> getMetalExceptionPage(Long id, String name, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<MetalException> idSpc = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<MetalException> nameSpc = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<MetalException> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		
		
    	Page<MetalException> page = metalExceptionRepository.findAll(idSpc.and(nameSpc).and(isDeletedFalse),pageable);
		return page;
	}
	
	
	
	 

}

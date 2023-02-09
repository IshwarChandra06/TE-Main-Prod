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
import com.eikona.tech.entity.LanyardType;
import com.eikona.tech.repository.LanyardTypeRepository;
import com.eikona.tech.service.LanyardTypeService;
import com.eikona.tech.util.GeneralSpecificationUtil;


@Service
public class LanyardTypeServiceImpl implements LanyardTypeService {

	@Autowired
	private LanyardTypeRepository lanyardTypeRepository;
	
	@Autowired
	private GeneralSpecificationUtil<LanyardType> generalSpecification;

	@Override
	public List<LanyardType> getAll() {
		return lanyardTypeRepository.findAllByIsDeletedFalse();
	}

	@Override
	public void save(LanyardType lanyardType) {
		lanyardType.setDeleted(false);
		this.lanyardTypeRepository.save(lanyardType);
	}

	@Override
	public LanyardType getById(long id) {
		Optional<LanyardType> optional = lanyardTypeRepository.findById(id);
		LanyardType lanyardType = null;
		if (optional.isPresent()) {
			lanyardType = optional.get();
		} else {
			throw new RuntimeException("Lanyard type not found for id "+ id);
		}
		return lanyardType;
	}

	@Override
	public void deleteById(long id) {
		Optional<LanyardType> optional = lanyardTypeRepository.findById(id);
		LanyardType lanyardType = null;
		if (optional.isPresent()) {
			lanyardType = optional.get();
			lanyardType.setDeleted(true);
		} else {
			throw new RuntimeException("Lanyard type not found for id "+ id);
		}
		this.lanyardTypeRepository.save(lanyardType);
	}

	@Override
	public PaginationDto<LanyardType> searchByField(Long id, String name, int pageno, String sortField,
			String sortDir) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<LanyardType> page = getLanyardTypePage(id, name, pageno, sortField, sortDir);
        List<LanyardType> lanyardTypeList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<LanyardType> dtoList = new PaginationDto<LanyardType>(lanyardTypeList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<LanyardType> getLanyardTypePage(Long id, String name, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<LanyardType> idSpc = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<LanyardType> nameSpc = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<LanyardType> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		
		
    	Page<LanyardType> page = lanyardTypeRepository.findAll(idSpc.and(nameSpc).and(isDeletedFalse),pageable);
		return page;
	}
	
	
	
	 

}

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
import com.eikona.tech.entity.Zone;
import com.eikona.tech.repository.ZoneRepository;
import com.eikona.tech.service.ZoneService;
import com.eikona.tech.util.GeneralSpecificationUtil;


@Service
public class ZoneServiceImpl implements ZoneService {

	@Autowired
	private ZoneRepository zoneRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Zone> generalSpecification;

	@Override
	public List<Zone> getAll() {
		return zoneRepository.findAllByIsDeletedFalse();
	}

	@Override
	public void save(Zone zone) {
		zone.setDeleted(false);
		this.zoneRepository.save(zone);
	}

	@Override
	public Zone getById(long id) {
		Optional<Zone> optional = zoneRepository.findById(id);
		Zone zone = null;
		if (optional.isPresent()) {
			zone = optional.get();
		} else {
			throw new RuntimeException("Zone not found for id "+ id);
		}
		return zone;
	}

	@Override
	public void deleteById(long id) {
		Optional<Zone> optional = zoneRepository.findById(id);
		Zone zone = null;
		if (optional.isPresent()) {
			zone = optional.get();
			zone.setDeleted(true);
		} else {
			throw new RuntimeException("Zone not found for id "+ id);
		}
		this.zoneRepository.save(zone);
	}

	@Override
	public PaginationDto<Zone> searchByField(Long id, String name, int pageno, String sortField,
			String sortDir) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Zone> page = getZonePage(id, name, pageno, sortField, sortDir);
        List<Zone> zoneList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Zone> dtoList = new PaginationDto<Zone>(zoneList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}

	private Page<Zone> getZonePage(Long id, String name, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<Zone> idSpc = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<Zone> nameSpc = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<Zone> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		
		
    	Page<Zone> page = zoneRepository.findAll(idSpc.and(nameSpc).and(isDeletedFalse),pageable);
		return page;
	}
	
	
	
	 

}

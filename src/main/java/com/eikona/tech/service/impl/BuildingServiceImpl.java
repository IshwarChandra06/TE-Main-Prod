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
import com.eikona.tech.constants.BuildingConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Building;
import com.eikona.tech.repository.BuildingRepository;
import com.eikona.tech.service.BuildingService;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class BuildingServiceImpl implements BuildingService{

	@Autowired
	private BuildingRepository buildingRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Building> generalSpecification;

	@Override
	public Building save(Building building) {
		building.setDeleted(false);
		this.buildingRepository.save(building);
		return this.buildingRepository.save(building);
	}

	@Override
	public Building getById(Long id) {
		Optional<Building> optional = buildingRepository.findById(id);
		Building building = null;
		if (optional.isPresent()) {
			building = optional.get();
		} else {
			throw new RuntimeException(BuildingConstants.PLANT_NOT_FOUND+ id);
		}
		return building;
	}
	
	@Override
	public List<Building> getAllByIsDeletedFalse() {
		return buildingRepository.findAllByIsDeletedFalse();
	}

	@Override
	public void deletedById(long id) {
		Optional<Building> optional = buildingRepository.findById(id);
		Building building = null;
		if (optional.isPresent()) {
			building = optional.get();
			building.setDeleted(true);
		} else {
			throw new RuntimeException(BuildingConstants.PLANT_NOT_FOUND + id);
		}
		this.buildingRepository.save(building);
		
	}

	@Override
	public PaginationDto<Building> searchByField(Long id, String name, String building, int pageno, String sortField,
			String sortDir) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Building> page = getBuildingPage(id, name, pageno, sortField, sortDir);
        List<Building> buildingList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Building> dtoList = new PaginationDto<Building>(buildingList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}
	
	private Page<Building> getBuildingPage(Long id, String name, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<Building> idSpc = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<Building> nameSpc = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<Building> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		
    	Page<Building> page = buildingRepository.findAll(idSpc.and(nameSpc).and(isDeletedFalse),pageable);
		return page;
	}

	@Override
	public List<Building> getAll() {
		return buildingRepository.findAllByIsDeletedFalse();
	}
	@Override
	public List<Building> getByPlant(String[] plants) {
		return buildingRepository.findByPlantCustom(plants);
	}
}

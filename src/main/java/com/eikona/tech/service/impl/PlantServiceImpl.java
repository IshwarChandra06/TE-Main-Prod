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
import com.eikona.tech.constants.PlantConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Plant;
import com.eikona.tech.repository.PlantRepository;
import com.eikona.tech.service.PlantService;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class PlantServiceImpl implements PlantService {
	
	@Autowired
	PlantRepository plantRepository;
	
	@Autowired
	private GeneralSpecificationUtil<Plant> generalSpecification;

	@Override
	public Plant save(Plant plant) {
		plant.setDeleted(false);
		this.plantRepository.save(plant);
		return this.plantRepository.save(plant);
	}

	@Override
	public Plant getById(Long id) {
		Optional<Plant> optional = plantRepository.findById(id);
		Plant plant = null;
		if (optional.isPresent()) {
			plant = optional.get();
		} else {
			throw new RuntimeException(PlantConstants.PLANT_NOT_FOUND+ id);
		}
		return plant;
	}
	
	@Override
	public List<Plant> getAll() {
		
		return plantRepository.findAllByIsDeletedFalse();
	}

	@Override
	public void deletedById(long id) {
		Optional<Plant> optional = plantRepository.findById(id);
		Plant plant = null;
		if (optional.isPresent()) {
			plant = optional.get();
			plant.setDeleted(true);
		} else {
			throw new RuntimeException(PlantConstants.PLANT_NOT_FOUND + id);
		}
		this.plantRepository.save(plant);
		
	}

	@Override
	public PaginationDto<Plant> searchByField(Long id, String name, String city, String pinCode, int pageno,
			String sortField, String sortDir) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<Plant> page = getPlantPage(id, name, city, pinCode, pageno, sortField, sortDir);
        List<Plant> plantList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<Plant> dtoList = new PaginationDto<Plant>(plantList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}
	
	private Page<Plant> getPlantPage(Long id, String name, String city, String pinCode, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<Plant> idSpc = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<Plant> nameSpc = generalSpecification.stringSpecification(name, ApplicationConstants.NAME);
		Specification<Plant> citySpc = generalSpecification.stringSpecification(city, PlantConstants.CITY);
		Specification<Plant> pinCodeSpc = generalSpecification.stringSpecification(pinCode, PlantConstants.PINCODE);
		Specification<Plant> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		
    	Page<Plant> page = plantRepository.findAll(idSpc.and(nameSpc).and(citySpc).and(pinCodeSpc).and(isDeletedFalse),pageable);
		return page;
	}

}

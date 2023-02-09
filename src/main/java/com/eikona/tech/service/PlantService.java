package com.eikona.tech.service;

import java.util.List;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Plant;

public interface PlantService {

	Plant save(Plant plant);

	Plant getById(Long id);

	void deletedById(long id);

	PaginationDto<Plant> searchByField(Long id, String name, String city, String pinCode, int pageno, String sortField,
			String sortDir);

	List<Plant> getAll();

}

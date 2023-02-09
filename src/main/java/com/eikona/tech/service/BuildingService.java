package com.eikona.tech.service;

import java.util.List;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Building;

public interface BuildingService {

	Building save(Building building);

	Building getById(Long id);

	void deletedById(long id);

	PaginationDto<Building> searchByField(Long id, String name, String plant, int pageno, String sortField,
			String sortDir);

	List<Building> getAllByIsDeletedFalse();

	List<Building> getAll();

	List<Building> getByPlant(String[] plants);

}

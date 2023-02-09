package com.eikona.tech.service;

import java.util.List;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.AccessLevel;

public interface AccessLevelService {

	PaginationDto<AccessLevel> searchByField(String name,String zone,String building,String plant, int pageno, String sortField, String sortDir);

	List<AccessLevel> getAll();

	AccessLevel save(AccessLevel accessLevel);

	AccessLevel getById(long id);

	void deletedById(long id);

	List<AccessLevel> getByPlantAndBuilding(String plant, String building);
}

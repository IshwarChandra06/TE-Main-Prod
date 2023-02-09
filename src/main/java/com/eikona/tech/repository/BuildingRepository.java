package com.eikona.tech.repository;

import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.Building;

@Repository
public interface BuildingRepository extends DataTablesRepository<Building, Long>{

	List<Building> findAllByIsDeletedFalse();

	@Query("select b from com.eikona.tech.entity.Building b where b.plant.name in :plants")
	List<Building> findByPlantCustom(String[] plants);

}

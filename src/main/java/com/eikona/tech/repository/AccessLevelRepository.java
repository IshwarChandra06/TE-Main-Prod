package com.eikona.tech.repository;

import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.AccessLevel;


@Repository
public interface AccessLevelRepository extends DataTablesRepository<AccessLevel, Long>{

	AccessLevel findByName(String name);

	AccessLevel findByAccessId(String acclevelId);

	@Query("select al from com.eikona.tech.entity.AccessLevel al where al.zone.id=:id")
	List<AccessLevel> findByZoneCustom(long id);
	
	@Query("select al from com.eikona.tech.entity.AccessLevel al where al.building.name = :building "
			+ "and al.building.plant.name = :plant")
	List<AccessLevel> findByPlantAndByildingCustom(String plant, String building);

}

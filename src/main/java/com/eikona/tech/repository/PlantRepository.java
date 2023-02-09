package com.eikona.tech.repository;

import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.Plant;

@Repository
public interface PlantRepository extends DataTablesRepository<Plant, Long>{


	List<Plant> findAllByIsDeletedFalse();

}

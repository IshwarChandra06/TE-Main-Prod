package com.eikona.tech.repository;


import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.Zone;



@Repository
public interface ZoneRepository extends DataTablesRepository<Zone, Long> {

	 List<Zone> findAllByIsDeletedFalse();
}

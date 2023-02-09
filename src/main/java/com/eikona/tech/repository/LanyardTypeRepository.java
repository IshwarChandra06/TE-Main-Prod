package com.eikona.tech.repository;


import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.LanyardType;



@Repository
public interface LanyardTypeRepository extends DataTablesRepository<LanyardType, Long> {

	 List<LanyardType> findAllByIsDeletedFalse();
	 
	 @Query("select e.name from com.eikona.tech.entity.LanyardType e where e.isDeleted=false")
	 List<String> findAllNameCustom();
}

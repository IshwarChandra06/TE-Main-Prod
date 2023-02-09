package com.eikona.tech.repository;


import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.MetalException;



@Repository
public interface MetalExceptionRepository extends DataTablesRepository<MetalException, Long> {

	 List<MetalException> findAllByIsDeletedFalse();
}

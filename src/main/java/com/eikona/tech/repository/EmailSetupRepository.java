package com.eikona.tech.repository;

import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.EmailSetup;

@Repository
public interface EmailSetupRepository extends DataTablesRepository<EmailSetup, Long>{

	List<EmailSetup> findAllByIsDeletedFalse();

}

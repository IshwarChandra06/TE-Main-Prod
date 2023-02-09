package com.eikona.tech.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.Privilege;

@Repository
public interface PrivilegeRepository extends DataTablesRepository<Privilege, Long> {

	List<Privilege> findAllByIsDeletedFalse();
	
	@Query("SELECT br from com.eikona.tech.entity.Privilege as br WHERE br.isDeleted=false")
	Page<Privilege> findAllData(Pageable pageable);
	
	@Query("select cm from com.eikona.tech.entity.Privilege cm where cm.isDeleted=false and (cm.name LIKE %:searchValue%)")
	List<Privilege> search(String searchValue);

}

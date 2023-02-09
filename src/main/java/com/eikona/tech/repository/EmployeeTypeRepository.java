package com.eikona.tech.repository;


import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.EmployeeType;



@Repository
public interface EmployeeTypeRepository extends DataTablesRepository<EmployeeType, Long> {


	 List<EmployeeType> findAllByIsDeletedFalse();

    EmployeeType findByNameAndIsDeletedFalse(String str);
    
    @Query("select e from com.eikona.tech.entity.EmployeeType e where e.name='Contractor' or e.name='Visitor'")
    List<EmployeeType> findContractorAndVisitorCustom();

    @Query("select e.name from com.eikona.tech.entity.EmployeeType e where e.isDeleted=false")
	List<String> findAllNameCustom();
	

}

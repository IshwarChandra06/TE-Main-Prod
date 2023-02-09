package com.eikona.tech.repository;


import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.Shift;

@Repository
public interface ShiftRepository extends DataTablesRepository<Shift, Long> {
	
	public List<Shift> findAllByIsDeletedFalse();

	public Shift findByNameAndIsDeletedFalse(String workScheduleExternalCode);

	public Shift findByDayAndName(String day, String workScheduleCode);


}

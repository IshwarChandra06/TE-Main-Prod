package com.eikona.tech.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.TemporaryTimeInfo;
@Repository
public interface TemporaryTimeInfoRepository  extends DataTablesRepository<TemporaryTimeInfo, Long> {

	 @Query("select tt from com.eikona.tech.entity.TemporaryTimeInfo tt where tt.employee.employeeId=:employeeId and tt.dateStr=:dateStr")
	 TemporaryTimeInfo findByEmployeeIdAndDateStrCustom(String employeeId, String dateStr);




}

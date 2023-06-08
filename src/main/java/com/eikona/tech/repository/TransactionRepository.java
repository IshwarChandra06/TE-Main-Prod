package com.eikona.tech.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.dto.TransactionDto;
import com.eikona.tech.entity.Transaction;

@Repository
public interface TransactionRepository extends DataTablesRepository<Transaction, Long> {

	@Query("select count(tr) from com.eikona.tech.entity.Transaction tr where tr.empId=:employeeId and tr.punchDate >= :startTime and tr.punchDate <= :endTime")
	Long findByDateAndEmpIdCustom(Date startTime, Date endTime, String employeeId);

	@Query("SELECT count(distinct tr.empId) FROM com.eikona.tech.entity.Transaction as tr "
			+ "where tr.punchDateStr =:dateStr and tr.deviceName=:name and tr.empId is not null")
	Long findEventCountByDateAndDeviceCustom(String dateStr, String name);

	@Query("SELECT  count(tr.name) FROM com.eikona.tech.entity.Transaction as tr "
			+ "where tr.punchDateStr =:dateStr and tr.deviceName=:name and tr.name='Unregistered'")
	Long findUnregisterCountByDateAndDeviceCustom(String dateStr, String name);

	@Query("SELECT new com.eikona.tech.dto.TransactionDto(tr.plant, count(distinct tr.empId)) FROM com.eikona.tech.entity.Transaction as tr "
			+ "where tr.punchDateStr =:dateStr and tr.empId is not null GROUP BY tr.plant")
	List<TransactionDto> findTransactionByPunchDateStrCustom(String dateStr);

	@Query("SELECT tr FROM com.eikona.tech.entity.Transaction as tr where tr.punchDate >=:startDate and tr.punchDate <=:endDate"
			+ " and tr.empId is not null order by tr.punchDateStr asc, tr.punchTimeStr asc")
	List<Transaction> getTransactionData(Date startDate, Date endDate);

	@Query("select tr from com.eikona.tech.entity.Transaction tr where tr.empId=:employeeId and tr.punchDate >= :startDate and tr.punchDate <= :endDate")
	List<Transaction> findByEmpIdAndDateCustom(String employeeId, Date startDate, Date endDate);

}

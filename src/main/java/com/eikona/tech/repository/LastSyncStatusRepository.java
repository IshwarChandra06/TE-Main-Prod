package com.eikona.tech.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.LastSyncStatus;

@Repository
public interface LastSyncStatusRepository extends DataTablesRepository<LastSyncStatus, Long> {

	LastSyncStatus findByActivity(String activity);

}

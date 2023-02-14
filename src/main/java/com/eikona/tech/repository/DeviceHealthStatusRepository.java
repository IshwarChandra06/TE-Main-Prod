package com.eikona.tech.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.DeviceHealthStatus;

@Repository
public interface DeviceHealthStatusRepository  extends DataTablesRepository<DeviceHealthStatus, Long>{

	@Query("select d from com.eikona.tech.entity.DeviceHealthStatus d where d.device.id=:id "
			+ "and d.date>=:startDate and d.date<=:endDate order by d.date asc")
	List<DeviceHealthStatus> findByDeviceIdAndDateCustom(long id, Date startDate, Date endDate);

	@Query("select d from com.eikona.tech.entity.DeviceHealthStatus d where d.device.id=:id "
			+ "and d.date>=:startDate and d.date<=:endDate and d.status=0 order by d.date asc")
	List<DeviceHealthStatus> findByOfflineDeviceIdAndDateCustom(Long id, Date startDate, Date endDate);

}

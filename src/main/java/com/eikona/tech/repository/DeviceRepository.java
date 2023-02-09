package com.eikona.tech.repository;

import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eikona.tech.entity.Device;
@Repository
public interface DeviceRepository extends DataTablesRepository<Device, Long>{

	Device findByName(String name);

	Device findByDoorId(String string);

	@Query("select d from com.eikona.tech.entity.Device d where d.isDeleted=false and d.accessLevel.building.name=:building")
	List<Device> findByBuildingAndIsDeletedFalseCustom(String building);

	List<Device> findAllByIsDeletedFalse();
	
	@Query("select d from com.eikona.tech.entity.Device d where d.isDeleted=false and d.accessLevel is not null and d.accessLevel.building is not null order by d.accessLevel.building.plant.name")
	List<Device> findAllByIsDeletedFalseOrderByPlantCustom();

	Device findBySerialNoAndIsDeletedFalse(String serialNo);
	

}



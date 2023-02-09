package com.eikona.tech.batch.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.eikona.tech.entity.Employee;

public class PersonSyncToBiosecurityMapper implements RowMapper<Employee> {

	@Override
	public Employee mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		Employee employee = new Employee();
		employee.setId(resultSet.getLong("id"));
		return employee;
	}

}

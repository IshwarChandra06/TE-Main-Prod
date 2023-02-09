package com.eikona.tech.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.eikona.tech.entity.Employee;

@Component
public class PersonSyncToBiosecurityProcessor implements ItemProcessor<Employee, Employee>{
	@Override
    public Employee process(Employee employee) throws Exception {
       Employee empShift = new Employee();
       empShift.setId(employee.getId());
       System.out.println("inside processor " + empShift.toString());
        return empShift;
    }
}

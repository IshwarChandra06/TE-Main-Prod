package com.eikona.tech.batch.job;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eikona.tech.batch.mapper.PersonSyncToBiosecurityMapper;
import com.eikona.tech.batch.processor.PersonSyncToBiosecurityProcessor;
import com.eikona.tech.batch.writer.PersonSyncToBiosecurityWriter;
import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.LastSyncStatus;
import com.eikona.tech.repository.LastSyncStatusRepository;

@Configuration
public class PersonSyncToBiosecurity {


    private JobBuilderFactory jobBuilderFactory;
    private LastSyncStatusRepository lastSyncStatusRepository;
    private StepBuilderFactory stepBuilderFactory;
    private PersonSyncToBiosecurityProcessor personSyncToBiosecurityProcessor;
    private DataSource dataSource;
    private PersonSyncToBiosecurityWriter personSyncToBiosecurityWriter;

    @Autowired
    public PersonSyncToBiosecurity(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, 
    		PersonSyncToBiosecurityProcessor personSyncToBiosecurityProcessor, DataSource dataSource,
    		PersonSyncToBiosecurityWriter personSyncToBiosecurityWriter,LastSyncStatusRepository lastSyncStatusRepository){
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.personSyncToBiosecurityProcessor = personSyncToBiosecurityProcessor;
        this.dataSource = dataSource;
        this.personSyncToBiosecurityWriter = personSyncToBiosecurityWriter;
        this.lastSyncStatusRepository=lastSyncStatusRepository;
    }

    @Qualifier(value = "personSyncToBiosecurity")
    @Bean
    public Job personSyncToBiosecurityJob() throws Exception {
        return this.jobBuilderFactory.get("personSyncToBiosecurity")
                .start(step1PersonSyncToBiosecurity())
                .build();
    }
    
    @Bean
    public Step step1PersonSyncToBiosecurity() throws Exception {
        return this.stepBuilderFactory.get("step1PersonSyncToBiosecurity")
    		.<Employee, Employee>chunk(100)
            .reader(personSyncToBiosecurityDBReader())
            .processor(personSyncToBiosecurityProcessor)
            .writer(personSyncToBiosecurityWriter)
            .build();
    }

    @Bean
    public ItemStreamReader<Employee> personSyncToBiosecurityDBReader() {
    	
    	LastSyncStatus lastSyncStatus= lastSyncStatusRepository.findByActivity("BS Employee Push");
    	
		SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_US_WITH_MILLISECOND);
        JdbcCursorItemReader<Employee> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        String startTime=dateFormat.format(new Date());
        if(null!=lastSyncStatus) 
        	   startTime=dateFormat.format(lastSyncStatus.getLastSyncTime());
        
              String endTime=dateFormat.format(new Date());
              
              reader.setSql("select * from te_employee_master em where em.last_modified_date <='"+endTime+"' and em.last_modified_date >='"+startTime+"'");
              reader.setRowMapper(new PersonSyncToBiosecurityMapper());
     
      
        return reader;
    }
    

    


}

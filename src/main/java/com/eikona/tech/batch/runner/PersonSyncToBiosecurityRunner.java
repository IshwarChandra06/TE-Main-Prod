package com.eikona.tech.batch.runner;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class PersonSyncToBiosecurityRunner {
	
    private JobLauncher simpleJobLauncher;
    
    @Qualifier("personSyncToBiosecurity")
    @Autowired
    private Job personSyncToBiosecurity;

    @Autowired
    public PersonSyncToBiosecurityRunner(Job personSyncToBiosecurity, JobLauncher jobLauncher) {
    	//super();
        this.simpleJobLauncher = jobLauncher;
        this.personSyncToBiosecurity = personSyncToBiosecurity;
    }
    
	
//	@Scheduled(cron = "0 0 0/4 * * ?")
	public void PersonSyncToBiosecurity() {
		runBatchJob();
	}

    @Async
    public void runBatchJob() {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addDate("date", new Date(), true);
        runJob(personSyncToBiosecurity, jobParametersBuilder.toJobParameters());
    }

    public void runJob(Job job, JobParameters parameters) {
        try {
            JobExecution jobExecution = simpleJobLauncher.run(job, parameters);
        }
        catch (Exception e) {
			e.printStackTrace();
		}
    }
}

package com.eikona.tech.service;

import java.util.List;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.EmailSetup;

public interface EmailSetupService {
	/**
	 * Returns all emailSetup List, which are isDeleted false.
	 * @param
	 */
	List<EmailSetup> getAll();
	/**
	 * This function saves the emailSetup in database according to the respective object.  
	 * @param 
	 */
    void save(EmailSetup emailSetup);
    /**
	 * This function retrieves the emailSetup from database according to the respective id.  
	 * @param
	 */
    EmailSetup getById(long id);
    /**
	 * This function deletes the emailSetup from database according to the respective id.  
	 * @param
	 */
    void deleteById(long id);
    
    
	PaginationDto<EmailSetup> searchByField(Long id, String subject,String to,String cc, int pageno, String sortField, String sortDir);
	
	void sendEmailAsAttachment( String fileName,EmailSetup emailSetup,String contentBody) throws Exception;
	
	void sendEmail(EmailSetup emailSetup,String contentBody) throws Exception;
	
	void sendEmailAsDualAttachment(String createdProfileFileName, String inactiveProfileFileName, EmailSetup emailSetup,
			String contentBody) throws Exception;
}

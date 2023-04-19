package com.eikona.tech.constants;

public class EmailSetupConstants {
	
	public static final String MONTHLY_ACCESS_REPORT  = "Dear %s,\n\n"+
			"Please find the Location name %s Monthly Access Report for the month of %s.\n"+
			"This is submitted for your review and actions in case of any abnormality observed.\n\n"+
			"This is a system generated report.\n\n\n"+
			"Regards,\n"+
			"Physical Security Team";
	
	public static final String MONTHLY_ACCESS_REPORT_TO_DEPARTMENT_HOD = "Dear %s,\n\n"+
			"Please find the Department name %s Monthly Access Report for the month of %s.\n"+
			"This is submitted for your review and actions in case of any abnormality observed.\n\n"+
			"This is a system generated report.\n\n\n"+
			"Regards,\n"+
			"Physical Security Team";
	
	public static final String GIVING_ACCESS = "Dear %s,\n\n"+
			"The following Access are successfully assigned to EMP ID %s , Name %s dated %s.\n%s\n\n"+
			"This is a system generated report.\n\n\n"+
			"Regards,\n"+
			"Physical Security Team";
	
	public static final String REVOKE_ACCESS = "Dear %s,\n\n"+
			"The following Access are successfully revoked from EMP ID %s , Name %s dated %s.\n%s\n\n"+
			"This is a system generated report.\n\n\n"+
			"Regards,\n"+
			"Physical Security Team";
	
	public static final String PROFILE_CREATION = "Dear %s,\n\n"+
			"Profile of EMP ID %s, Name %s is successfully created in the application dated %s.\n\n"+
			"This is a system generated report.\n\n\n"+
			"Regards,\n"+
			"Physical Security Team";
	
	public static final String BLACK_LISTING = "Dear %s,\n\n"+
			"Profile of EMP ID %s, Name %s is successfully Blacklisted in the application dated %s with\n"+
			"remarks %s.\n\n"+
			"This is a system generated report.\n\n\n"+
			"Regards,\n"+
			"Physical Security Team";
	
	public static final String SUSPENSION = "Dear %s,\n\n"+
			"Profile of EMP ID %s, Name %s is successfully Suspended in the application from %s to\n"+
			"%s with remarks %s.\n\n"+
			"This is a system generated report.\n\n\n"+
			"Regards,\n"+
			"Physical Security Team";
	
	public static final String FAILED_TRANSACTION = "Dear %s,\n\n"+
			"Please find the department wise %s Monthly failed transaction Report for the month of\n"+
			"%s. This is submitted for your review and actions in case of any abnormality observed.\n\n"+
			"Kindly review in case of access amendments are required and contact to Access control team for modification.\n\n"+
			"This is a system generated report.\n\n\n"+
			"Regards,\n"+
			"Physical Security Team";
}

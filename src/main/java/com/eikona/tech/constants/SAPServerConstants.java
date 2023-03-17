package com.eikona.tech.constants;

public class SAPServerConstants {
	
	public static final String  D ="d";
	public static final String  RESULTS="results";
	public static final String  LABEL="label";
	public static final String  USER_NAV="userNav";
	public static final String  HOSTEL_FACILITY_NAV="externalCodeOfcust_HostelFacilityNav";
	public static final String  DEPARTMENT_NAV="departmentNav";
	public static final String  EMPLOYEEMENT_NAV="employmentNav";
	public static final String  EMPLOYEE_STATUS_NAV="emplStatusNav";
	public static final String  PERSON_NAV="personNav";
	public static final String  MANAGER_EMPLOYMENT_NAV="managerEmploymentNav";
	public static final String  PERSONAL_INFO_NAV="personalInfoNav";
	public static final String  WORK_SCHEDULE_CODE_NAV="workscheduleCodeNav";
	public static final String  WORK_SCHEDULE_NAV="workScheduleNav";
	public static final String  EMAIL_NAV = "emailNav";
	public static final String  WORK_SCHEDULE_DAY_MODELS="workScheduleDayModels";
	public static final String  PICK_LIST_LABEL="picklistLabels";
	public static final String  START_DATE="startDate";
	public static final String  HIRE_DATE="hireDate";
	public static final String  END_DATE="endDate";
	public static final String  DAY_MODEL_NAV="dayModelNav";
	public static final String  DEFAULT_FULL_NAME="defaultFullName";
	public static final String  CATEGORY="category";
	public static final String  START_TIME="startTime";
	public static final String  END_TIME="endTime";
	public static final String  EXTERNAL_NAME_DEFAULT_VALUE="externalName_defaultValue";
	public static final String  SEGMENTS="segments";
	public static final String  USER_ID="userId";
	public static final String  MANAGER_ID="managerId";
	public static final String  EMAIL_ADDRESS="emailAddress";
	public static final String  CUSTOM_STRING_2="customString2";
	public static final String  CUSTOM_STRING_7="customString7";
	public static final String  PAY_GRADE="payGrade";
	public static final String  DAY="day";
	public static final String  DAY_MODEL="dayModel";
	public static final String  NAME="name";
	public static final String  HOSTEL_NAME="cust_HostelName";
	public static final String  WARDEN_NAME="cust_Hostel_Warden_Name";
	public static final String  WARDEN_MOBILE_NO="cust_Hostel_warden_Mobile_number";
	public static final String  WARDEN_EMAIL="cust_Hostel_Warden_email_id";
	public static final String  FIRST_NAME="firstName";
	public static final String  MIDDLE_NAME="middleName";
	public static final String  LAST_NAME="lastName";
	public static final String  WORK_SCHEDULE_EXTERNAL_CODE="WorkSchedule_externalCode";
	public static final String  SCHEDULED_WORKING_TIME="SCHEDULED_WORKING_TIME";
	public static final String  OFF="OFF";
	public static final String  TIME_FORMAT_CONVERSION ="%d:%02d:%02d";
	
	public static final String  SHIFT_MASTER_DATA_API="https://api44.sapsf.com/odata/v2/WorkSchedule?$select=crossMidnightAllowed,externalCode,"
			+ "workScheduleDayModels/WorkSchedule_externalCode,workScheduleDayModels/dayModelNav/externalCode,workScheduleDayModels/dayModelNav/nonWorkingDay,"
			+ "workScheduleDayModels/dayModelNav/segments/endTime,workScheduleDayModels/dayModelNav/segments/startTime,workScheduleDayModels/dayModelNav/segments/category,"
			+ "workScheduleDayModels/category,workScheduleDayModels/dayModel,workScheduleDayModels/day&$expand=workScheduleDayModels/dayModelNav/segments"
			+ "&$format=json&$filter=externalCode in 'First_shift_Pattern','Second_shift_Pattern','Third_shift_Pattern','IND_GenShift','Flexi_General_Pattern'";
	
	public static final String  EMPLOYEE_MASTER_DATA_API="https://api44.sapsf.com/odata/v2/EmpJob?$select=managerId,employmentNav/empJobRelationshipNav/relUserNav/firstName,employmentNav/empJobRelationshipNav/relationshipTypeNav/externalCode,"
			+ "managerEmploymentNav/personNav/emailNav/emailAddress,managerEmploymentNav/personNav/personalInfoNav/firstName,managerEmploymentNav/personNav/personalInfoNav/lastName,"
			+ "managerEmploymentNav/personNav/personalInfoNav/middleName,seqNumber,startDate,userId,userNav/externalCodeOfcust_Bus_ServiceNav/cust_Nodal_Point,"
			+ "userNav/externalCodeOfcust_HostelFacilityNav/cust_HostelName,departmentNav/name,emplStatusNav/picklistLabels/label, "
			+ "employmentNav/personNav/personalInfoNav/firstName,employmentNav/personNav/personalInfoNav/lastName,endDate,event, eventNav/picklistLabels/label,"
			+ "eventReason,eventReasonNav/name,lastModifiedDateTime,seqNumber,startDate,userId, customString7,payGrade,hireDate,customString2,"
			+ "userNav/externalCodeOfcust_HostelFacilityNav/cust_Hostel_Warden_Name,userNav/externalCodeOfcust_HostelFacilityNav/cust_Hostel_Warden_email_id,"
			+ "userNav/externalCodeOfcust_HostelFacilityNav/cust_Hostel_warden_Mobile_number&$expand=departmentNav,emplStatusNav/picklistLabels,"
			+ " employmentNav/personNav/personalInfoNav,eventNav/picklistLabels,eventReasonNav,employmentNav/empJobRelationshipNav/relUserNav,"
			+ "employmentNav/empJobRelationshipNav/relationshipTypeNav,managerEmploymentNav/personNav/emailNav,managerEmploymentNav/personNav/personalInfoNav,"
			+ "userNav/externalCodeOfcust_Bus_ServiceNav,userNav/externalCodeOfcust_HostelFacilityNav&$format=json&$top=%s&$skip=%s";
	
	public static final String  EMPLOYEE_SHIFT_INFO_API="https://api44.sapsf.com/odata/v2/EmpJob?$format=json&$select=userId,workscheduleCode,"
			+ "workscheduleCodeNav/workScheduleDayModels,workscheduleCodeNav/workScheduleDayModels/dayModelNav/segments&$expand=workscheduleCodeNav/workScheduleDayModels,"
			+ "workscheduleCodeNav/workScheduleDayModels/dayModelNav/segments&$format=json&$top=%s&$skip=%s";
	
	public static final String  EMPLOYEE_SHIFT_INFO_BY_DATE_API="https://api44.sapsf.com/odata/v2/EmpJob?$format=json&$select=userId,workscheduleCode,"
			+ "workscheduleCodeNav/workScheduleDayModels,workscheduleCodeNav/workScheduleDayModels/dayModelNav/segments&$expand=workscheduleCodeNav/workScheduleDayModels,"
			+ "workscheduleCodeNav/workScheduleDayModels/dayModelNav/segments&$filter=lastModifiedDateTime ge '%s' and lastModifiedDateTime le '%s' and emplStatusNav/externalCode eq 'A'&$format=json&$top=%s&$skip=%s";
	
	public static final String EMPLOYEE_MASTER_DATA_BY_DATE_API = "https://api44.sapsf.com/odata/v2/EmpJob?$select=managerId,employmentNav/empJobRelationshipNav/relUserNav/firstName,employmentNav/empJobRelationshipNav/relationshipTypeNav/externalCode,"
			+ "managerEmploymentNav/personNav/emailNav/emailAddress,managerEmploymentNav/personNav/personalInfoNav/firstName,managerEmploymentNav/personNav/personalInfoNav/lastName,"
			+ "managerEmploymentNav/personNav/personalInfoNav/middleName,seqNumber,startDate,userId,userNav/externalCodeOfcust_Bus_ServiceNav/cust_Nodal_Point,"
			+ "userNav/externalCodeOfcust_HostelFacilityNav/cust_HostelName,departmentNav/name,emplStatusNav/picklistLabels/label, "
			+ "employmentNav/personNav/personalInfoNav/firstName,employmentNav/personNav/personalInfoNav/lastName,endDate,event, eventNav/picklistLabels/label,"
			+ "eventReason,eventReasonNav/name,lastModifiedDateTime,seqNumber,startDate,userId, customString7,payGrade,hireDate,customString2,"
			+ "userNav/externalCodeOfcust_HostelFacilityNav/cust_Hostel_Warden_Name,userNav/externalCodeOfcust_HostelFacilityNav/cust_Hostel_Warden_email_id,"
			+ "userNav/externalCodeOfcust_HostelFacilityNav/cust_Hostel_warden_Mobile_number&$expand=departmentNav,emplStatusNav/picklistLabels,"
			+ " employmentNav/personNav/personalInfoNav,eventNav/picklistLabels,eventReasonNav,employmentNav/empJobRelationshipNav/relUserNav,"
			+ "employmentNav/empJobRelationshipNav/relationshipTypeNav,managerEmploymentNav/personNav/emailNav,managerEmploymentNav/personNav/personalInfoNav,"
			+ "userNav/externalCodeOfcust_Bus_ServiceNav,userNav/externalCodeOfcust_HostelFacilityNav&$filter=lastModifiedDateTime ge '%s' and lastModifiedDateTime le '%s'&$format=json&$top=%s&$skip=%s";
	
	public static final String TEMPORARY_TIME_INFO_API = "https://api44.sapsf.com/odata/v2/TemporaryTimeInformation?$format=json&$expand=workScheduleNav,"
			+ "workScheduleNav/workScheduleDayModels,workScheduleNav/workScheduleDayModels/dayModelNav,workScheduleNav/workScheduleDayModels/dayModelNav/segments&$select=startDate,"
			+ "endDate,userId,workScheduleNav/workScheduleDayModels,workScheduleNav/workScheduleDayModels/dayModelNav,workScheduleNav/workScheduleDayModels/dayModelNav/segments&$format=json&$top=%s&$skip=%s";
	
	public static final String TEMPORARY_TIME_INFO_BY_DATE_API = "https://api44.sapsf.com/odata/v2/TemporaryTimeInformation?$format=json&$expand=workScheduleNav,"
			+ "workScheduleNav/workScheduleDayModels,workScheduleNav/workScheduleDayModels/dayModelNav,workScheduleNav/workScheduleDayModels/dayModelNav/segments&$select=startDate,"
			+ "endDate,userId,workScheduleNav/workScheduleDayModels,workScheduleNav/workScheduleDayModels/dayModelNav,workScheduleNav/workScheduleDayModels/dayModelNav/segments&$format=json&$top=%s&$skip=%s"
			+ "&$filter=startDate ge datetimeoffset'%s' and startDate le datetimeoffset'%s'";
	
	
}

package com.eikona.tech.constants;

public class BioSecurityConstants {
	public static final String  API_ADD_PERSON ="/api/person/add";
	
	public static final String  MESSAGE ="message";
	public static final String  DATA ="data";
	
	public static final String  ACC_LEVEL_SYNC_API ="/api/accLevel/list?pageNo=1&pageSize=1000&access_token=";
	public static final String  DOOR_SYNC_API ="/api/door/list?pageNo=1&pageSize=1000&access_token=";
	public static final String API_GET_PERSON = "/api/person/get/%s";
	
	public static final String  PERSON_ADD_JSON ="{\n"
						+ "  \"name\": \"%s\",\n"
						+ "  \"pin\": \"%s\",\n"
						+ "  \"cardNo\": \"%s\",\n"
						+ "  \"accLevelIds\": \"%s\",\n"
						+ "  \"hireDate\": \"%s\",\n"
                        + "  \"lastName\": \"%s\"\n"
						+ "}";
	
	public static final String  PERSON_ACCESS_TIME_JSON ="{\n"
						+ "  \"accEndTime\": \"%s\",\n"
						+ "  \"accStartTime\": \"%s\",\n"
						+ "  \"accLevelIds\": \"%s\",\n"
						+ "  \"cardNo\": \"%s\",\n"
						+ "  \"pin\":  \"%s\"\n"
						+ "}";

	
}

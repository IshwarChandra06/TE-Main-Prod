package com.eikona.tech.util;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.BioSecurityConstants;
import com.eikona.tech.entity.Employee;


@Component
public class BioSecurityServerUtil {

	@Value("${biosecurity.host.url}")
	private String host;

	@Value("${biosecurity.api.accesstoken}")
	private String accesstoken;

	@Value("${biosecurity.server.port}")
	private String serverPort;
	
	@Value("${biosecurity.db.port}")
	private String dbPort;
	
	@Value("${biosecurity.db.name}")
	private String dbName;
	
	@Value("${biosecurity.db.url}")
	private String dbHost;
	
	@Value("${biosecurity.db.username}")
	private String dbUsername;
	
	@Value("${biosecurity.db.password}")
	private String dbPassword;

	@Autowired
	private RequestExecutionUtil requestExecutionUtil;
	
	public ResultSet jdbcConnection(String query) {
		
		ResultSet resultSet = null;
		try {
			
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		
			//Creating a connection to the database
			Connection conn = DriverManager.getConnection("jdbc:sqlserver://"+dbHost+":"+dbPort+";databaseName="+dbName+";encrypt=true;trustServerCertificate=true",dbUsername,dbPassword);
			
			//Executing SQL query and fetching the result
			Statement st = conn.createStatement();
			resultSet = st.executeQuery(query);
				
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}	
		
		return resultSet;
	}

	public String addShiftTimeAndAccessLevelToPerson(Employee employee,String accStartTime,String accEndTime) {
		try {
			String accessLevelIds="";
			String cardId="";
			if(null!=employee.getAccesslevelIds()&&!employee.getAccesslevelIds().isEmpty()) 
				accessLevelIds=employee.getAccesslevelIds();
			if(null!=employee.getCardId()) 
				cardId=employee.getCardId();
			
			String myjson = BioSecurityConstants.PERSON_ACCESS_TIME_JSON.formatted(accEndTime,accStartTime,accessLevelIds,cardId, employee.getEmployeeId());
			System.out.println(myjson);
			String myurl = ApplicationConstants.HTTP_COLON_DOUBLE_SLASH + host + ApplicationConstants.DELIMITER_COLON
					+ serverPort + BioSecurityConstants.API_ADD_PERSON + ApplicationConstants.DELIMITER_QUESTION_MARK
					+ ApplicationConstants.ACCESS_TOKEN + ApplicationConstants.DELIMITER_EQUAL_TO + accesstoken;
			HttpPost request = getHttpPostRequest(myjson, myurl);
			

			String responeData = requestExecutionUtil.executeHttpPostRequest(request);

			String message = getMessageFromResponse(responeData);

			return message;
		} catch (Exception e) {
			e.printStackTrace();
			return ApplicationConstants.FAILED;
		}

	}

	public String addEmployeeToBioSecurity(Employee employee) throws Exception {
		
			SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
			String joinDate="";
			String accessLevelIds="";
			String cardId="";
			String firstName="";
			String lastName="";
			if(null!=employee.getJoinDate()) 
				joinDate=dateFormat.format(employee.getJoinDate());
			if(null!=employee.getAccesslevelIds()&&!employee.getAccesslevelIds().isEmpty()) 
				accessLevelIds=employee.getAccesslevelIds();
			if(null!=employee.getCardId()) 
				cardId=employee.getCardId();
			if(null!=employee.getFirstName()) 
				firstName=employee.getFirstName();
			if(null!=employee.getLastName()) 
				lastName=employee.getLastName();
			String myjson = BioSecurityConstants.PERSON_ADD_JSON.formatted(firstName, employee.getEmployeeId(),cardId,accessLevelIds,
						joinDate,lastName);
			
			System.out.println(myjson);
			String myurl = ApplicationConstants.HTTP_COLON_DOUBLE_SLASH + host + ApplicationConstants.DELIMITER_COLON
					+ serverPort + BioSecurityConstants.API_ADD_PERSON + ApplicationConstants.DELIMITER_QUESTION_MARK
					+ ApplicationConstants.ACCESS_TOKEN + ApplicationConstants.DELIMITER_EQUAL_TO + accesstoken;
			HttpPost request = getHttpPostRequest(myjson, myurl);

			String responeData = requestExecutionUtil.executeHttpPostRequest(request);
			
			System.out.println("Push into biosecurity Successfully !!");

			String message = getMessageFromResponse(responeData);

			return message;

	}

	private String getMessageFromResponse(String responeData) throws ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
		String message = (String) jsonResponse.get(BioSecurityConstants.MESSAGE);
		return message;
	}

	private HttpPost getHttpPostRequest(String myjson, String myurl) throws UnsupportedEncodingException {
		HttpPost request = new HttpPost(myurl);
		StringEntity entity = new StringEntity(myjson);
		request.setHeader(ApplicationConstants.HEADER_CONTENT_TYPE, ApplicationConstants.APPLICATION_JSON);
		request.setEntity(entity);
		return request;
	}

	public JSONObject getEmployeeFromBioSecurity(String pin) throws Exception {
		
		String myurl = ApplicationConstants.HTTP_COLON_DOUBLE_SLASH + host + ApplicationConstants.DELIMITER_COLON
				+ serverPort + BioSecurityConstants.API_GET_PERSON.formatted(pin) + ApplicationConstants.DELIMITER_QUESTION_MARK
				+ ApplicationConstants.ACCESS_TOKEN + ApplicationConstants.DELIMITER_EQUAL_TO + accesstoken;
		 HttpGet request = new HttpGet(myurl);
			
		    String responeData =requestExecutionUtil.executeHttpGetRequest(request);
		    JSONParser jsonParser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
			JSONObject dataObject = (JSONObject) jsonResponse.get(BioSecurityConstants.DATA);
			
			return dataObject;
	}

}

package com.eikona.tech.export;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmployeeConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.entity.Employee;

@Component
public class ExportMetalExceptionCsvUtil {

public String csvGenerator(HttpServletResponse response, List<Employee> employeeList) throws ParseException, IOException {
		
		CellProcessor[] processors = new CellProcessor[] {
	           
	            new NotNull(), 
	            new Optional(),
	            new Optional(),
	            new Optional()
	           
		};
		
		
		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_UNDERSCORE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Metal_Exception_Report_" + currentDateTime + ApplicationConstants.EXTENSION_CSV ;
		
		 ICsvBeanWriter beanWriter = null;
		 try {
	        beanWriter = new CsvBeanWriter(new FileWriter(filename), CsvPreference.STANDARD_PREFERENCE);
	        
	        String[] header = {
	        		 HeaderConstants.EMPLOYEE_ID, HeaderConstants.FIRST_NAME,
	        		HeaderConstants.LAST_NAME, HeaderConstants.METAL_EXCEPTION
	        	};
	        
	        String[] field = {
	        		EmployeeConstants.EMPLOYEE_ID,EmployeeConstants.FIRST_NAME,
	        		EmployeeConstants.LAST_NAME,EmployeeConstants.METAL_EXCEPTION,
	        	};
	        
	        beanWriter.writeHeader(header);
	 
	        for (Employee employee : employeeList) {
	            beanWriter.write(employee, field, processors);
	        }
	 
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	        if (beanWriter != null) {
	            try {
	                beanWriter.close();
	            } catch (IOException e) {
	            	e.printStackTrace();
	            }
	        }
	    }
		return filename;
	}
}

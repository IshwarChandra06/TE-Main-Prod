package com.eikona.tech.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmailScheduleConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.EmailLogs;
import com.eikona.tech.entity.EmployeeShiftInfo;
import com.eikona.tech.repository.EmailLogsRepository;
import com.eikona.tech.repository.EmployeeRepository;
import com.eikona.tech.repository.EmployeeShiftInfoRepository;
import com.eikona.tech.util.CalendarUtil;

@Service
@EnableScheduling
public class ShiftReportServiceImpl {
	
	@Autowired
	private EmployeeShiftInfoRepository employeeShiftInfoRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private EmailLogsRepository emailLogsRepository;
	
	@Autowired
	private CalendarUtil calendarUtil;
	
	@Value("${tepl.from.mail}")
	private  String fromMail;
	
	@Value("${tepl.mail.password}")
	private  String password;
	
//	@Scheduled(fixedDelay = 50000)
	@Scheduled(cron="0 15 * * 6 *")
	public void sendEmailToManager() throws ParseException {
		
		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		List<String> distinctManagerEmailList = employeeRepository.findDistinctByManagerEmailCustom();
		String dateStr = dateFormat.format(new Date());
		Date startDate=calendarUtil.getNextOrPreviousDate(dateFormat.parse(dateStr), 2, 0, 0, 0);
		Date endDate=calendarUtil.getNextOrPreviousDate(dateFormat.parse(dateStr), 7, 0, 0, 0);
		
		String sDate=dateFormat.format(startDate);
		String eDate=dateFormat.format(endDate);
		String managerSubject ="TEPL Shift Report for Manager From "+sDate+" to "+eDate;
		
		
		List<EmailLogs> emailLogList=sendMailToManager(distinctManagerEmailList, startDate, endDate, managerSubject);
		
		emailLogsRepository.saveAll(emailLogList);
	}
	@Scheduled(cron="30 15 * * 6 *")
	public void sendEmailToWarden() throws ParseException {
		
		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		List<String> distinctHostelWardenEmailList = employeeRepository.findDistinctByHostelWardenEmailCustom();
		String dateStr = dateFormat.format(new Date());
		Date startDate=calendarUtil.getNextOrPreviousDate(dateFormat.parse(dateStr), 2, 0, 0, 0);
		Date endDate=calendarUtil.getNextOrPreviousDate(dateFormat.parse(dateStr), 7, 0, 0, 0);
		
		String sDate=dateFormat.format(startDate);
		String eDate=dateFormat.format(endDate);
		String hostelSubject ="TEPL Shift Report for Hostel Warden From "+sDate+" to "+eDate;
		
		
		List<EmailLogs> emailLogList=sendMailToHostelWarden(distinctHostelWardenEmailList, startDate, endDate, hostelSubject);
		
		emailLogsRepository.saveAll(emailLogList);
	}

	private List<EmailLogs> sendMailToHostelWarden(List<String> distinctHostelWardenEmailList, Date startDate, Date endDate,String hostelSubject) {
		List<EmailLogs> emailLogList= new ArrayList<>();
		for(String wardenEmail:distinctHostelWardenEmailList) {
			EmailLogs emailLogs= new EmailLogs();
			List<EmployeeShiftInfo> employeeShiftInfoList=employeeShiftInfoRepository.findByNextSevenDaysShiftInfoHostelCustom(startDate,endDate,wardenEmail);
			try {
				String fileName=generateShiftReportExcel(employeeShiftInfoList);
				sendEmail(hostelSubject, fileName,wardenEmail);
				
				emailLogs.setDate(new Date());
				emailLogs.setManagerEmailId(wardenEmail);
				emailLogs.setType("Shift Report For Hostel Warden");
				emailLogList.add(emailLogs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return emailLogList;
	}

	private List<EmailLogs> sendMailToManager(List<String> distinctManagerEmailList, Date startDate, Date endDate,String managerSubject) {
		List<EmailLogs> emailLogList= new ArrayList<>();
		for(String managerEmail:distinctManagerEmailList) {
			EmailLogs emailLogs= new EmailLogs();
			List<EmployeeShiftInfo> employeeShiftInfoList=employeeShiftInfoRepository.findByNextSevenDaysShiftInfoCustom(startDate,endDate,managerEmail);
			try {
				String fileName=generateShiftReportExcel(employeeShiftInfoList);
				sendEmail(managerSubject, fileName,managerEmail);
				
				emailLogs.setDate(new Date());
				emailLogs.setManagerEmailId(managerEmail);
				emailLogs.setType("Shift Report For Manager");
				emailLogList.add(emailLogs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return emailLogList;
	}
	
	private String generateShiftReportExcel(List<EmployeeShiftInfo> employeeShiftInfoList) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_UNDERSCORE);
		String currentDateTime = dateFormat.format(new Date());
		File theDir = new File(EmailScheduleConstants.EXCEL_ROOTPATH);
		if (!theDir.exists()){
		    theDir.mkdirs();
		}
		
		String filename = theDir + EmailScheduleConstants.EXCEL_FILE_NAME + currentDateTime + ApplicationConstants.EXTENSION_EXCEL ;
		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		int rowCount =  NumberConstants.ZERO;
		Row row = sheet.createRow(rowCount++);

		Font font = workBook.createFont();
		font.setBold(true);

		CellStyle cellStyle = setBorderStyle(workBook, BorderStyle.THICK, font);
	
		//set head for excel
		setHeadForExcel(row, cellStyle);
		
		font = workBook.createFont();
		font.setBold(false);
		cellStyle = setBorderStyle(workBook, BorderStyle.THIN, font);
		
		//set data for excel
		setDataForExcel(employeeShiftInfoList, sheet, rowCount, cellStyle);

		FileOutputStream fileOut = new FileOutputStream(filename);
		workBook.write(fileOut);
		fileOut.close();
		workBook.close();

		return filename;
	}
	private CellStyle setBorderStyle(Workbook workBook, BorderStyle borderStyle, Font font) {
		CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setBorderTop(borderStyle);
		cellStyle.setBorderBottom(borderStyle);
		cellStyle.setBorderLeft(borderStyle);
		cellStyle.setBorderRight(borderStyle);
		cellStyle.setFont(font);
		return cellStyle;
	}

	private void setDataForExcel(List<EmployeeShiftInfo> employeeShiftList, Sheet sheet, int rowCount,
			CellStyle cellStyle) {
		DateFormat timeFormat = new SimpleDateFormat(ApplicationConstants.TIME_FORMAT_24HR);
		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_OF_US);
		for (EmployeeShiftInfo employeeShiftInfo : employeeShiftList) {
			Row row = sheet.createRow(rowCount++);
	
			int columnCount = NumberConstants.ZERO;
			
			Cell cell = row.createCell(columnCount++);
			cell.setCellValue(dateFormat.format(employeeShiftInfo.getDate()));
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			cell.setCellValue(employeeShiftInfo.getEmployee().getEmployeeId());
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getFirstName())
				cell.setCellValue(employeeShiftInfo.getEmployee().getFirstName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getLastName())
				cell.setCellValue(employeeShiftInfo.getEmployee().getLastName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getManagerId())
				cell.setCellValue(employeeShiftInfo.getEmployee().getManagerId());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getManagerName())
				cell.setCellValue(employeeShiftInfo.getEmployee().getManagerName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getDepartment())
				cell.setCellValue(employeeShiftInfo.getEmployee().getDepartment());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getDesignation())
				cell.setCellValue(employeeShiftInfo.getEmployee().getDesignation());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getHostelName())
				cell.setCellValue(employeeShiftInfo.getEmployee().getHostelName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getHostelWardenName())
				cell.setCellValue(employeeShiftInfo.getEmployee().getHostelWardenName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getEetoName())
				cell.setCellValue(employeeShiftInfo.getEmployee().getEetoName());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getBusNo())
				cell.setCellValue(employeeShiftInfo.getEmployee().getBusNo());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEmployee().getNodalPoint())
				cell.setCellValue(employeeShiftInfo.getEmployee().getNodalPoint());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getShift())
				cell.setCellValue(employeeShiftInfo.getShift());
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getStartTime())
				cell.setCellValue(timeFormat.format(employeeShiftInfo.getStartTime()));
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			cell = row.createCell(columnCount++);
			if(null!=employeeShiftInfo.getEndTime())
				cell.setCellValue(timeFormat.format(employeeShiftInfo.getEndTime()));
			else
				cell.setCellValue(ApplicationConstants.DELIMITER_EMPTY);
			cell.setCellStyle(cellStyle);
	
			
	
		}
	}

	private void setHeadForExcel(Row row, CellStyle cellStyle) {
		
		Cell cell = row.createCell( NumberConstants.ZERO);
		cell.setCellValue(HeaderConstants.DATE);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell( NumberConstants.ONE);
		cell.setCellValue(HeaderConstants.EMPLOYEE_ID);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell( NumberConstants.TWO);
		cell.setCellValue(HeaderConstants.FIRST_NAME);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell( NumberConstants.THREE);
		cell.setCellValue(HeaderConstants.LAST_NAME);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell( NumberConstants.FOUR);
		cell.setCellValue(HeaderConstants.MANAGER_ID);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIVE);
		cell.setCellValue(HeaderConstants.MANAGER_NAME);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell(NumberConstants.SIX);
		cell.setCellValue(HeaderConstants.DEPARTMENT);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell(NumberConstants.SEVEN);
		cell.setCellValue(HeaderConstants.DESIGNATION);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell(NumberConstants.EIGHT);
		cell.setCellValue(HeaderConstants.HOSTEL_NAME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.NINE);
		cell.setCellValue(HeaderConstants.HOSTEL_WARDEN_NAME);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell(NumberConstants.TEN);
		cell.setCellValue(HeaderConstants.EETO_NAME);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell(NumberConstants.ELEVEN);
		cell.setCellValue(HeaderConstants.BUS_NO);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell(NumberConstants.TWELVE);
		cell.setCellValue(HeaderConstants.NODAL_POINT);
		cell.setCellStyle(cellStyle);
	
		cell = row.createCell(NumberConstants.THIRTEEN);
		cell.setCellValue(HeaderConstants.SHIFT);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FOURTEEN);
		cell.setCellValue(HeaderConstants.SHIFT_IN_TIME);
		cell.setCellStyle(cellStyle);
		
		cell = row.createCell(NumberConstants.FIFTEEN);
		cell.setCellValue(HeaderConstants.SHIFT_OUT_TIME);
		cell.setCellStyle(cellStyle);
	
	}
	private void sendEmail(String mailSubject, String fileName,String toMail) throws Exception{
		try {
			final String subject = mailSubject;
			final String body = "Hi Dear,\n"+"Please find the Attachment for the upcoming week shift report.";
			final String fromEmail = fromMail;
			final String pass = password;
			final String toEmail = toMail;
			
			Properties properties = setEmailProperties();
			
	        //create Authenticator object to pass in Session.getInstance argument
			Authenticator auth = new Authenticator() {
				//override the getPasswordAuthentication method
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(fromEmail, pass);
				}
			};
			Session session = Session.getInstance(properties, auth);
			MimeMessage msg = new MimeMessage(session);
			
			setMimeMessage(fileName,subject,body,toEmail,msg);
			
			System.out.println("Message is ready");
			
			Transport.send(msg);
			
			System.out.println("Email Sent Successfully!!");
	    }
		catch (AddressException e) {
            throw new AddressException(EmailScheduleConstants.INCORRECT_EMAIL_ADDRESS);

        } catch (MessagingException e) {
            throw new MessagingException(EmailScheduleConstants.AUTHENTICATION_FAILED);

        } 
		catch (Exception e) {
            throw  new Exception(EmailScheduleConstants.ERROR_IN_METHOD + e.getMessage());
        }
	}

	private  void setMimeMessage(String fileName, final String subject, final String body, final String toEmail,
			MimeMessage msg) throws MessagingException, AddressException {
		
		String nameOfFile=fileName.substring( fileName.lastIndexOf('/')+1, fileName.length() );
		//set message headers
		msg.addHeader(ApplicationConstants.HEADER_CONTENT_TYPE, EmailScheduleConstants.TEXT_OR_HTML);
		msg.addHeader(EmailScheduleConstants.FORMAT, EmailScheduleConstants.FLOWED);
		msg.addHeader(EmailScheduleConstants.CONTENT_TRANSFER_ENCODING, EmailScheduleConstants.EIGHT_BIT);
		msg.setFrom(new InternetAddress(fromMail));
		
		msg.setReplyTo(InternetAddress.parse(fromMail, false));
		
//		InternetAddress[] myBccList = InternetAddress.parse("shravan@eikona.tech");
//		msg.setRecipients(Message.RecipientType.BCC,myBccList);

		// Set Subject: header field
		msg.setSubject(subject);

		// Create the message part 
		BodyPart messageBodyPart = new MimeBodyPart();

		// Fill the message
		messageBodyPart.setText(body);

		// Create a multipar message
		Multipart multipart = new MimeMultipart();

		// Set text message part
		multipart.addBodyPart(messageBodyPart);

		// Part two is attachment
		messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(fileName);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(nameOfFile);
		multipart.addBodyPart(messageBodyPart);

		// Send the complete message parts
		msg.setContent(multipart);

		msg.setSentDate(new Date());

		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
	}

	private static Properties setEmailProperties() {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.starttls.required", "true");
		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		return properties;
	}
}

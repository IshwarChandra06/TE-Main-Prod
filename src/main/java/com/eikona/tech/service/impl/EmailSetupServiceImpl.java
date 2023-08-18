package com.eikona.tech.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.EmailScheduleConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.EmailSetup;
import com.eikona.tech.repository.EmailSetupRepository;
import com.eikona.tech.service.EmailSetupService;
import com.eikona.tech.util.GeneralSpecificationUtil;

@Service
public class EmailSetupServiceImpl implements EmailSetupService{
	
	@Autowired
	EmailSetupRepository emailSetupRepository;
	
	@Autowired
	private GeneralSpecificationUtil<EmailSetup> generalSpecification;
	
	@Value("${tepl.from.mail}")
	private  String fromMail;
	
	@Value("${tepl.mail.password}")
	private  String password;

	@Override
	public void save(EmailSetup emailSetup) {
		emailSetup.setDeleted(false);
		this.emailSetupRepository.save(emailSetup);
	}

	@Override
	public EmailSetup getById(long id) {
		Optional<EmailSetup> optional = emailSetupRepository.findById(id);
		EmailSetup emailSetup = null;
		if (optional.isPresent()) {
			emailSetup = optional.get();
		} else {
			throw new RuntimeException("Email setup not found for id" + id);
		}
		return emailSetup;
	}
	
	@Override
	public List<EmailSetup> getAll() {
		
		return emailSetupRepository.findAllByIsDeletedFalse();
	}

	@Override
	public void deleteById(long id) {
		Optional<EmailSetup> optional = emailSetupRepository.findById(id);
		EmailSetup emailSetup = null;
		if (optional.isPresent()) {
			emailSetup = optional.get();
			emailSetup.setDeleted(true);
		} else {
			throw new RuntimeException("Email setup not found for id" + id);
		}
		this.emailSetupRepository.save(emailSetup);
		
	}

	@Override
	public PaginationDto<EmailSetup> searchByField(Long id, String subject,String to,String cc, int pageno,
			String sortField, String sortDir) {
		if (null == sortDir || sortDir.isEmpty()) {
			sortDir =  ApplicationConstants.ASC;
		}
		if (null == sortField || sortField.isEmpty()) {
			sortField = ApplicationConstants.ID;
		}
		Page<EmailSetup> page = getEmailSetupPage(id, subject, to, cc, pageno, sortField, sortDir);
        List<EmailSetup> emailSetupList =  page.getContent();
		
		sortDir = (ApplicationConstants.ASC.equalsIgnoreCase(sortDir))?ApplicationConstants.DESC:ApplicationConstants.ASC;
		PaginationDto<EmailSetup> dtoList = new PaginationDto<EmailSetup>(emailSetupList, page.getTotalPages(),
				page.getNumber() + NumberConstants.ONE, page.getSize(), page.getTotalElements(), page.getTotalElements(), sortDir, ApplicationConstants.SUCCESS, ApplicationConstants.MSG_TYPE_S);
		return dtoList;
	}
	
	private Page<EmailSetup> getEmailSetupPage(Long id, String subject,String to,String cc, int pageno, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageno - NumberConstants.ONE, NumberConstants.TEN, sort);
		
		Specification<EmailSetup> idSpc = generalSpecification.longSpecification(id, ApplicationConstants.ID);
		Specification<EmailSetup> subjectSpc = generalSpecification.stringSpecification(subject, "subject");
		Specification<EmailSetup> toSpc = generalSpecification.stringSpecification(to, "to");
		Specification<EmailSetup> ccSpc = generalSpecification.stringSpecification(cc, "cc");
		Specification<EmailSetup> isDeletedFalse = generalSpecification.isDeletedSpecification(false);
		
    	Page<EmailSetup> page = emailSetupRepository.findAll(idSpc.and(subjectSpc).and(toSpc).and(ccSpc).and(isDeletedFalse),pageable);
		return page;
	}
	
	@Override
	public void sendEmail(EmailSetup emailSetup,String contentBody) throws Exception {
			final String body =contentBody;
			final String fromEmail = fromMail;
			final String pass = password;
			
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
			
			setMimeMessage(emailSetup,body,msg);
			
			System.out.println("Message is ready");
			
			Transport.send(msg);
			
			System.out.println("Email Sent Successfully!!");
	}
	private  void setMimeMessage(EmailSetup emailSetup, final String body, MimeMessage msg) throws MessagingException, AddressException {
			
			String toMail=emailSetup.getTo();
			//set message headers
			msg.addHeader(ApplicationConstants.HEADER_CONTENT_TYPE, EmailScheduleConstants.TEXT_OR_HTML);
			msg.addHeader(EmailScheduleConstants.FORMAT, EmailScheduleConstants.FLOWED);
			msg.addHeader(EmailScheduleConstants.CONTENT_TRANSFER_ENCODING, EmailScheduleConstants.EIGHT_BIT);
			msg.setFrom(new InternetAddress(fromMail));
			
			msg.setReplyTo(InternetAddress.parse(fromMail, false));
			
			InternetAddress[] myCcList = InternetAddress.parse(emailSetup.getCc());
			msg.setRecipients(Message.RecipientType.CC,myCcList);
			
			// Set Subject: header field
			msg.setSubject(emailSetup.getSubject(), "UTF-8");
	
			msg.setText(body, "UTF-8");
	
			msg.setSentDate(new Date());
	
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toMail, false));
		}
	
	@Override
	public void sendEmailAsAttachment( String fileName,EmailSetup emailSetup,String contentBody) throws Exception {
			final String body =contentBody;
			final String fromEmail = fromMail;
			final String pass = password;
			
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
			
			setMimeMessageAttachment(fileName,emailSetup,body,msg);
			
			System.out.println("Message is ready");
			
			Transport.send(msg);
			
			System.out.println("Email Sent Successfully!!");
	}
	private void setMimeMessageAttachment(String fileName, EmailSetup emailSetup, String body, MimeMessage msg)  throws MessagingException, AddressException {
		
		String nameOfFile=fileName.substring( fileName.lastIndexOf('/')+1, fileName.length() );
		//set message headers
		msg.addHeader(ApplicationConstants.HEADER_CONTENT_TYPE, EmailScheduleConstants.TEXT_OR_HTML);
		msg.addHeader(EmailScheduleConstants.FORMAT, EmailScheduleConstants.FLOWED);
		msg.addHeader(EmailScheduleConstants.CONTENT_TRANSFER_ENCODING, EmailScheduleConstants.EIGHT_BIT);
		msg.setFrom(new InternetAddress(fromMail));
		
		msg.setReplyTo(InternetAddress.parse(fromMail, false));
		
		// Set Subject: header field
		msg.setSubject(emailSetup.getSubject());

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

		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailSetup.getTo(), false));
	}
	@Override
	public void sendEmailAsDualAttachment(String createdProfileFileName, String inactiveProfileFileName,
			EmailSetup emailSetup, String contentBody) throws Exception{
			final String body =contentBody;
			final String fromEmail = fromMail;
			final String pass = password;
			
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
			
			setMimeMessageDualAttachment(createdProfileFileName,inactiveProfileFileName,emailSetup,body,msg);
			
			System.out.println("Message is ready");
			
			Transport.send(msg);
			
			System.out.println("Email Sent Successfully!!");
	}
  private void setMimeMessageDualAttachment(String createdProfileFileName, String inactiveProfileFileName, EmailSetup emailSetup, String body, MimeMessage msg)  throws Exception {
		
		
		//set message headers
		msg.addHeader(ApplicationConstants.HEADER_CONTENT_TYPE, EmailScheduleConstants.TEXT_OR_HTML);
		msg.addHeader(EmailScheduleConstants.FORMAT, EmailScheduleConstants.FLOWED);
		msg.addHeader(EmailScheduleConstants.CONTENT_TRANSFER_ENCODING, EmailScheduleConstants.EIGHT_BIT);
		msg.setFrom(new InternetAddress(fromMail));
		
		msg.setReplyTo(InternetAddress.parse(fromMail, false));
		
		// Set Subject: header field
		msg.setSubject(emailSetup.getSubject());

		// Create the message part 
		BodyPart messageBodyPart = new MimeBodyPart();

		// Fill the message
		messageBodyPart.setText(body);

		// Create a multipar message
		Multipart multipart = new MimeMultipart();

		// Set text message part
		multipart.addBodyPart(messageBodyPart);

		// Part two is attachment
		if(!createdProfileFileName.isEmpty())
		  addAttachment(multipart,createdProfileFileName);
		if(!inactiveProfileFileName.isEmpty())
		  addAttachment(multipart,inactiveProfileFileName);

		// Send the complete message parts
		msg.setContent(multipart);

		msg.setSentDate(new Date());

		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailSetup.getTo(), false));
	}
  
  private static void addAttachment(Multipart multipart, String filename) throws Exception
  {
      DataSource source = new FileDataSource(filename);
      BodyPart messageBodyPart = new MimeBodyPart();        
      messageBodyPart.setDataHandler(new DataHandler(source));
      String nameOfFile=filename.substring( filename.lastIndexOf('/')+1, filename.length() );
      messageBodyPart.setFileName(nameOfFile);
      multipart.addBodyPart(messageBodyPart);
  }
	private static Properties setEmailProperties() {
		Properties properties = new Properties();
		
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "outlook.office365.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
		return properties;
	}

	
}

package com.eikona.tech.export;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.HeaderConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.entity.Employee;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
@Component
public class ExportMetalExceptionPdfUtil {
	
	
	public String pdfGenerator(HttpServletResponse response, List<Employee> employeeList) throws ParseException, IOException {

		DateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_TIME_FORMAT_OF_INDIA_SPLIT_BY_UNDERSCORE);
		String currentDateTime = dateFormat.format(new Date());
		String filename = "Metal_Exception_Report_" + currentDateTime + ApplicationConstants.EXTENSION_PDF;
		Document document = new Document(PageSize.A1);
		PdfWriter.getInstance(document, new FileOutputStream(filename));

		document.open();
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(NumberConstants.EIGHTEEN);
		font.setColor(Color.BLUE);

		Paragraph p = new Paragraph("Metal Exception Report", font);
		p.setAlignment(Paragraph.ALIGN_CENTER);

		document.add(p);

		PdfPTable table = new PdfPTable(NumberConstants.FOUR);
		table.setWidthPercentage(100f);
		table.setSpacingBefore(NumberConstants.TEN);

		writeTableHeader(table);
		for (Employee employee : employeeList) {
			writeTableData(table, employee);
		}

		document.add(table);
		document.close();
		
		FileOutputStream fileOut = new FileOutputStream(filename);
		PdfWriter.getInstance(document, fileOut);
		ServletOutputStream outputStream = response.getOutputStream();
		PdfWriter.getInstance(document, outputStream);
		fileOut.close();
		
	return filename;
	}
	
	private void writeTableHeader(PdfPTable table) {
		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(Color.BLUE);
		cell.setPadding(NumberConstants.FIVE);

		Font font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setColor(Color.WHITE);

		cell.setPhrase(new Phrase(HeaderConstants.EMPLOYEE_ID, font));
		table.addCell(cell);

		cell.setPhrase(new Phrase(HeaderConstants.FIRST_NAME, font));
		table.addCell(cell);

		cell.setPhrase(new Phrase(HeaderConstants.LAST_NAME, font));
		table.addCell(cell);

		cell.setPhrase(new Phrase(HeaderConstants.METAL_EXCEPTION, font));
		table.addCell(cell);

	}

	private void writeTableData(PdfPTable table, Employee employee) {
		table.addCell(employee.getEmployeeId());
		table.addCell(employee.getFirstName());
		table.addCell(employee.getLastName());
//		table.addCell(employee.getMetalException());
		
	}
}

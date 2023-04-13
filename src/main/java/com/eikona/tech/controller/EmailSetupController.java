package com.eikona.tech.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.EmailSetup;
import com.eikona.tech.service.EmailSetupService;

@Controller
public class EmailSetupController {
	
	@Autowired
	private EmailSetupService emailSetupService;
	
	@GetMapping(value={"/email-setup"})
	@PreAuthorize("hasAuthority('email_setup_view')")
	public String emailSetupList(Model model) {
		return "emailSetup/emailSetup_list";
	}
	
	@GetMapping("/email-setup/new")
	@PreAuthorize("hasAuthority('email_setup_create')")
	public String newEmailSetup(Model model) {
		model.addAttribute("emailSetup", new EmailSetup());
		model.addAttribute("title", "New Email Setup");
		return "emailSetup/emailSetup_new";
	}
	
	@PostMapping("/email-setup/add")
	@PreAuthorize("hasAnyAuthority('email_setup_create','email_setup_update')")
	public String saveEmailSetup(@ModelAttribute("emailSetup") EmailSetup emailSetup, @Valid EmailSetup ar, Errors errors, String title,
			Model model) {
		if (errors.hasErrors()) {
			model.addAttribute("emailSetup", emailSetup);
			model.addAttribute("title", title);
			return "emailSetup/emailSetup_new";
		} else {
			if (null == emailSetup.getId())
				emailSetupService.save(emailSetup);
			else {
				EmailSetup emailSetupObj = emailSetupService.getById(emailSetup.getId());
				emailSetup.setCreatedBy(emailSetupObj.getCreatedBy());
				emailSetup.setCreatedDate(emailSetupObj.getCreatedDate());
				emailSetupService.save(emailSetup);
			}
			return "redirect:/email-setup";
		}

	}
	
	@GetMapping("/email-setup/edit/{id}")
	@PreAuthorize("hasAuthority('email_setup_update')")
	public String updateEmailSetup(@PathVariable(value = "id") long id, Model model) {
		EmailSetup emailSetup = emailSetupService.getById(id);
		model.addAttribute("emailSetup", emailSetup);
		model.addAttribute("title", "Update Email Setup");
		return "emailSetup/emailSetup_new";
	}

	@GetMapping("/email-setup/delete/{id}")
	@PreAuthorize("hasAuthority('email_setup_delete')")
	public String deleteEmailSetup(@PathVariable(value = "id") long id) {

		this.emailSetupService.deleteById(id);
		return "redirect:/email-setup";
	}

	@RequestMapping(value = "/search/email-setup", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('email_setup_view')")
	public @ResponseBody PaginationDto<EmailSetup> search(Long id, String name, String city,  String pinCode, int pageno, String sortField,
			String sortDir) {

		PaginationDto<EmailSetup> dtoList = emailSetupService.searchByField(id, name, city, pinCode, pageno, sortField, sortDir);
		return dtoList;
	}
}

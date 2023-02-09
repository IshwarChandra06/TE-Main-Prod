package com.eikona.tech.config.audit;


import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {
		
		return (SecurityContextHolder.getContext().getAuthentication() == null)? Optional.of("System") :Optional.of(SecurityContextHolder.getContext().getAuthentication().getName());
		
	}

    @Bean
    public AuditorAware<String> auditorProvider() {
		return () -> getCurrentAuditor();
    }
    
    /*
    if you are using spring security, you can get the currently logged username with following code segment.

    SecurityContextHolder.getContext().getAuthentication().getName()
    
   */
    

    
}

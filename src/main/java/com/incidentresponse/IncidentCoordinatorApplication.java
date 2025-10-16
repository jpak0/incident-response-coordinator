package com.incidentresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication // Enables Spring Boot auto-configuration and component scanning
@EnableScheduling      // Enables auto-escalation scheduled job, runs every 60 seconds.
public class IncidentCoordinatorApplication {

	/**
	 * Main method to start the application.
	 * @param args the command line arguments
	 */
    public static void main(String[] args) {
        SpringApplication.run(IncidentCoordinatorApplication.class, args);
    }
}

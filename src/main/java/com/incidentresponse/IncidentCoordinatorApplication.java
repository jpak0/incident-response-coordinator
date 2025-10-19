package com.incidentresponse;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class IncidentCoordinatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentCoordinatorApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Incident Response Coordinator API")
                        .version("1.0.0")
                        .description("""
                                ## Cloud-Native Incident Management Platform
                                
                                Production-ready REST API for enterprise incident response workflow management.
                                
                                ### Key Features
                                - **Automated Escalation**: Priority escalation for unacknowledged incidents
                                - **State Machine Workflow**: Enforced incident lifecycle transitions
                                - **Complete Audit Trail**: Every action logged with timestamp and actor
                                - **Real-time Coordination**: Multi-user collaboration with comments
                                
                                ### Technology Stack
                                - Spring Boot 3.3.5
                                - Spring Data JPA / Hibernate
                                - H2 Database (Development) / PostgreSQL (Production)
                                - Docker & Kubernetes Ready
                                - AWS Deployable
                                
                                ### Workflow States
                                `REPORTED → ACKNOWLEDGED → INVESTIGATING → MITIGATING → RESOLVED → CLOSED`
                                
                                ---
                                Built for secure, self-hosted deployment in enterprise environments.
                                """)
                        .contact(new Contact()
                                .name("Joseph Pak")
                                .email("joseph.pak@example.com")
                                .url("https://github.com/jpak0/incident-response-coordinator"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.incident-coordinator.example.com")
                                .description("Production Server (Example)")
                ));
    }
}
package com.meetingroom.room.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI roomServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Meeting Room Microservice API")
                        .description("REST API specifications for Meeting Room management, facilities, and image URL attachments")
                        .version("1.0.0")
                        .contact(new Contact().name("MeetingRoom Engineering Team").email("engineering@meetingroom.com")));
    }
}

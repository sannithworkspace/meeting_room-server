package com.meetingroom.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Meeting Room Booking Microservice API")
                        .description("REST API specifications for Meeting Room Reservations, Collision Checks, Cancellations, and Available Room Discovery")
                        .version("1.0.0")
                        .contact(new Contact().name("MeetingRoom Engineering Team").email("engineering@meetingroom.com")));
    }
}

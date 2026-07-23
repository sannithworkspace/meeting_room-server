package com.meetingroom.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendActivationOtpRequest {

    @Email
    @NotBlank
    private String email;
}

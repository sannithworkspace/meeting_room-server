package com.meetingroom.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {
    private String email;
    private String otp;
    private String fullName;
    private String type; // e.g. "ACTIVATION" or "PASSWORD_RESET"
}

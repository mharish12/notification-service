package com.notificationservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailSenderDto {

    private Long id;

    @NotBlank(message = "Sender name is required")
    private String name;

    @NotBlank(message = "SMTP host is required")
    private String host;

    @NotNull(message = "SMTP port is required")
    @Min(value = 1, message = "Port must be greater than 0")
    private Integer port;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private JsonNode properties;

    private Boolean isActive = true;
}
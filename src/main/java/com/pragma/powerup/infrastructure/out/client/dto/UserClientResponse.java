package com.pragma.powerup.infrastructure.out.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserClientResponse {
    private Long id;
    private String role;
    private String phone;
    private String email;
}

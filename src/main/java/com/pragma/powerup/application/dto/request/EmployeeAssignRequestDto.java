package com.pragma.powerup.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeAssignRequestDto {
    @Schema(example = "3", description = "Id (en usuarios-ms) de un usuario con rol de empleado")
    private Long employeeId;
}

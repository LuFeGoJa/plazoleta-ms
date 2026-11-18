package com.pragma.powerup.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = false)
public class DishStatusRequestDto {
    @Schema(example = "false", description = "true habilita el plato, false lo deshabilita")
    private Boolean active;
}

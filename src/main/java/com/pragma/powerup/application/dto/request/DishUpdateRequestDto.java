package com.pragma.powerup.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/** Solo se pueden modificar precio y descripción: cualquier otro campo se rechaza con 400. */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = false)
public class DishUpdateRequestDto {
    @Schema(example = "35000", description = "Opcional. Entero positivo mayor a 0")
    private Integer price;
    @Schema(example = "Ahora con aguacate y plátano maduro", description = "Opcional. No puede ir vacía")
    private String description;
}

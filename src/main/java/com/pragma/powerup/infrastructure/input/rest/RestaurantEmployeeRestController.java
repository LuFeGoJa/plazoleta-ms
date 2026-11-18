package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.EmployeeAssignRequestDto;
import com.pragma.powerup.application.handler.IRestaurantEmployeeHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurant")
@RequiredArgsConstructor
public class RestaurantEmployeeRestController {

    private final IRestaurantEmployeeHandler employeeHandler;

    @Operation(summary = "Vincular un empleado a un restaurante (apoyo a la HU12). Solo el PROPIETARIO del restaurante",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empleado vinculado", content = @Content),
            @ApiResponse(responseCode = "400", description = "El usuario no existe o no es empleado",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403", description = "No es el propietario de ese restaurante",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "El restaurante no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "El empleado ya está vinculado a un restaurante",
                    content = @Content)
    })
    @PostMapping("/{restaurantId}/employee")
    public ResponseEntity<Void> assignEmployee(@PathVariable Long restaurantId,
                                               @RequestBody EmployeeAssignRequestDto request) {
        employeeHandler.assignEmployee(restaurantId, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}

package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantListItemResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurant")
@RequiredArgsConstructor
public class RestaurantRestController {

    private final IRestaurantHandler restaurantHandler;

    @Operation(summary = "Crear un restaurante (HU2). Solo ADMIN",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurante creado", content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Datos inválidos, o el id no corresponde a un propietario", content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es administrador", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe un restaurante con ese NIT",
                    content = @Content),
            @ApiResponse(responseCode = "503", description = "El servicio de usuarios no responde",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> saveRestaurant(@RequestBody RestaurantRequestDto restaurantRequestDto) {
        restaurantHandler.saveRestaurant(restaurantRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Listar los restaurantes por orden alfabético, paginados (HU9). Solo CLIENTE",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Página de restaurantes (solo nombre y logo), ordenados por nombre"),
            @ApiResponse(responseCode = "400", description = "page negativo, o size fuera del rango 1 a 100",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es cliente", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PageResponseDto<RestaurantListItemResponseDto>> listRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(restaurantHandler.listRestaurants(page, size));
    }
}

package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.response.DishListItemResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dish")
@RequiredArgsConstructor
public class DishRestController {

    private final IDishHandler dishHandler;

    @Operation(summary = "Crear un plato (HU3). Solo el PROPIETARIO del restaurante",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plato creado (activo por defecto)",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría inexistente",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "No es propietario, o no es el propietario de ese restaurante",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "El restaurante no existe", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> saveDish(@RequestBody DishRequestDto dishRequestDto) {
        dishHandler.saveDish(dishRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Modificar el precio y/o la descripción de un plato (HU4). Solo el PROPIETARIO del restaurante",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plato modificado", content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Precio no entero o <= 0, descripción vacía, sin campos, o campos no permitidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "No es propietario, o el plato es de un restaurante de otro propietario",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "El plato no existe", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateDish(@PathVariable Long id,
                                           @RequestBody DishUpdateRequestDto dishUpdateRequestDto) {
        dishHandler.updateDish(id, dishUpdateRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Habilitar o deshabilitar un plato (HU7). Solo el PROPIETARIO del restaurante",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del plato actualizado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Falta active, o no es true/false, o trae otros campos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "No es propietario, o el plato es de un restaurante de otro propietario",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "El plato no existe", content = @Content)
    })
    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> changeDishStatus(@PathVariable Long id,
                                                 @RequestBody DishStatusRequestDto dishStatusRequestDto) {
        dishHandler.changeDishStatus(id, dishStatusRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Listar los platos activos de un restaurante, paginados y con filtro opcional por categoría (HU10). Solo CLIENTE",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Página de platos activos, ordenados por categoría y nombre"),
            @ApiResponse(responseCode = "400",
                    description = "Falta restaurantId, page negativo, size fuera de 1 a 100 o categoría inexistente",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es cliente", content = @Content),
            @ApiResponse(responseCode = "404", description = "El restaurante no existe", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PageResponseDto<DishListItemResponseDto>> listDishes(
            @RequestParam Long restaurantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(dishHandler.listDishes(restaurantId, categoryId, page, size));
    }
}

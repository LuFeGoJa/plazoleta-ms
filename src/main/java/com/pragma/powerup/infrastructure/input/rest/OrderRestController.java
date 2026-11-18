package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.OrderDeliveryRequestDto;
import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.OrderSummaryResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.domain.model.OrderStatus;
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
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderRestController {

    private final IOrderHandler orderHandler;

    @Operation(summary = "Realizar un pedido (HU11). Solo CLIENTE",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado en estado PENDIENTE"),
            @ApiResponse(responseCode = "400",
                    description = "Datos inválidos, plato de otro restaurante o plato inactivo",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es cliente", content = @Content),
            @ApiResponse(responseCode = "404", description = "El restaurante o un plato no existe",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "El cliente ya tiene un pedido en proceso",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        return new ResponseEntity<>(orderHandler.createOrder(orderRequestDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Listar los pedidos de un estado del restaurante del empleado, paginados (HU12). Solo EMPLEADO",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Página de pedidos del estado pedido, del más antiguo al más reciente"),
            @ApiResponse(responseCode = "400",
                    description = "Estado inválido o ausente, page negativo o size fuera de 1 a 100",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "No es empleado, o no está vinculado a ningún restaurante", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PageResponseDto<OrderSummaryResponseDto>> listOrdersByStatus(
            @RequestParam OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderHandler.listOrdersByStatus(status, page, size));
    }

    @Operation(summary = "Asignarse un pedido pendiente: pasa a EN_PREPARACION con el empleado como responsable (HU13). Solo EMPLEADO",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido asignado, ahora EN_PREPARACION"),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "No es empleado, no está vinculado a un restaurante, o el pedido es de otro restaurante",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "El pedido no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "El pedido ya no está PENDIENTE", content = @Content)
    })
    @PatchMapping("/{id}/assign")
    public ResponseEntity<OrderSummaryResponseDto> assignOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderHandler.assignOrderToEmployee(id));
    }

    @Operation(summary = "Marcar un pedido como LISTO y enviar el PIN de entrega al cliente por mensaje (HU14). Solo EMPLEADO",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido LISTO; el PIN se envió al celular del cliente"),
            @ApiResponse(responseCode = "400",
                    description = "El cliente no tiene celular, o no está en formato internacional",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "No es empleado, no está vinculado a un restaurante, o el pedido es de otro restaurante",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "El pedido no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "El pedido no está EN_PREPARACION", content = @Content),
            @ApiResponse(responseCode = "503",
                    description = "No se pudo enviar el mensaje: el pedido no cambia de estado", content = @Content)
    })
    @PatchMapping("/{id}/ready")
    public ResponseEntity<OrderSummaryResponseDto> markOrderReady(@PathVariable Long id) {
        return ResponseEntity.ok(orderHandler.markOrderReady(id));
    }

    @Operation(summary = "Entregar un pedido LISTO validando el PIN que recibió el cliente (HU15). Solo EMPLEADO",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PIN correcto: el pedido queda ENTREGADO"),
            @ApiResponse(responseCode = "400", description = "PIN ausente o incorrecto", content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "No es empleado, no está vinculado a un restaurante, o el pedido es de otro restaurante",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "El pedido no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "El pedido no está LISTO", content = @Content)
    })
    @PatchMapping("/{id}/deliver")
    public ResponseEntity<OrderSummaryResponseDto> deliverOrder(@PathVariable Long id,
                                                                @RequestBody OrderDeliveryRequestDto request) {
        return ResponseEntity.ok(orderHandler.deliverOrder(id, request));
    }

    @Operation(summary = "Cancelar un pedido propio mientras esté PENDIENTE (HU16). Solo CLIENTE",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido CANCELADO"),
            @ApiResponse(responseCode = "401", description = "Sin sesión iniciada", content = @Content),
            @ApiResponse(responseCode = "403", description = "No es cliente, o el pedido es de otro cliente",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "El pedido no existe", content = @Content),
            @ApiResponse(responseCode = "409",
                    description = "El pedido ya no está PENDIENTE (por ejemplo: \"Lo sentimos, tu pedido ya está en preparación y no puede cancelarse\")",
                    content = @Content)
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderSummaryResponseDto> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderHandler.cancelOrder(id));
    }
}

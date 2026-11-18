package com.pragma.powerup.domain.exception;

public class EmployeeWithoutRestaurantException extends DomainException {
    public EmployeeWithoutRestaurantException() {
        super("El empleado no está vinculado a ningún restaurante");
    }
}

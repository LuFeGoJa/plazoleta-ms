package com.pragma.powerup.domain.exception;

public class EmployeeAlreadyAssignedException extends DomainException {
    public EmployeeAlreadyAssignedException(Long employeeId) {
        super("El empleado " + employeeId + " ya está vinculado a un restaurante");
    }
}

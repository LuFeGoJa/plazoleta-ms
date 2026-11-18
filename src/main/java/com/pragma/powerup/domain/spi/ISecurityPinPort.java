package com.pragma.powerup.domain.spi;

public interface ISecurityPinPort {

    /** Genera un PIN de seguridad de 6 dígitos. */
    String generate();
}

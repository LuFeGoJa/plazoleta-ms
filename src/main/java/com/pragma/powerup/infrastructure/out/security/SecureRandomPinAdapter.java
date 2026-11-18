package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.spi.ISecurityPinPort;

import java.security.SecureRandom;

/** Genera PIN de 6 dígitos con un generador criptográficamente seguro. */
public class SecureRandomPinAdapter implements ISecurityPinPort {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}

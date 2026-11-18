package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.spi.ICurrentUserPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Lee el id de usuario que JwtAuthenticationFilter dejó en el contexto de seguridad. */
public class SecurityContextCurrentUserAdapter implements ICurrentUserPort {

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Long) {
            return (Long) authentication.getDetails();
        }
        return null;
    }
}

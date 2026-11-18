package com.pragma.powerup.domain.spi;

/** Da acceso al usuario autenticado que hace la petición actual. */
public interface ICurrentUserPort {

    /** Id del usuario autenticado, o null si no se puede determinar. */
    Long getCurrentUserId();
}

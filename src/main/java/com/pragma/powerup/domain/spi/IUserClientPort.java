package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.UserInfoModel;

import java.util.Optional;

/** Puerto de salida hacia el microservicio de Usuarios. */
public interface IUserClientPort {

    /** Devuelve vacío si el usuario no existe; lanza UserServiceUnavailableException si el servicio no responde. */
    Optional<UserInfoModel> findUserById(Long id);
}

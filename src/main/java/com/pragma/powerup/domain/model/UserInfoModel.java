package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos mínimos de un usuario que Plazoleta necesita conocer (viven en usuarios-ms). */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoModel {
    private Long id;
    private String role;
    /** Celular del usuario en formato internacional; puede ser null si usuarios-ms no lo informa. */
    private String phone;
    /** Correo del usuario; se guarda en la trazabilidad de los pedidos. */
    private String email;

    public UserInfoModel(Long id, String role) {
        this(id, role, null, null);
    }

    public UserInfoModel(Long id, String role, String phone) {
        this(id, role, phone, null);
    }
}

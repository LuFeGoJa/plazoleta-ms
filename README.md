# plazoleta-ms · Plazoleta de comidas (PRAGMA)

Microservicio de **Plazoleta**: restaurantes, platos y pedidos.
Arquitectura hexagonal sobre `powerup-arquetipo` (Gradle, Java 11, Spring Boot 2.7.3, MySQL).

## Historias de usuario
| HU | Descripción | Estado |
|----|-------------|--------|
| 2 | Crear restaurante (`POST /api/v1/restaurant`, solo ADMIN) | Hecha |
| 3 | Crear plato (`POST /api/v1/dish`, solo PROPIETARIO del restaurante) | Hecha |
| 18 | Eficiencia: tiempos y ranking (se consulta en trazabilidad-ms, solo PROPIETARIO) | Hecha |
| 17 | Trazabilidad de los pedidos (registro en trazabilidad-ms) | Hecha |
| 16 | Cancelar pedido (`PATCH /api/v1/order/{id}/cancel`, solo CLIENTE) | Hecha |
| 15 | Entregar pedido con PIN (`PATCH /api/v1/order/{id}/deliver`, solo EMPLEADO) | Hecha |
| 14 | Pedido listo + PIN por mensaje (`PATCH /api/v1/order/{id}/ready`, solo EMPLEADO) | Hecha |
| 13 | Asignarse a un pedido (`PATCH /api/v1/order/{id}/assign`, solo EMPLEADO) | Hecha |
| 12 | Listar pedidos por estado (`GET /api/v1/order?status=`, solo EMPLEADO) + vincular empleado (`POST /api/v1/restaurant/{id}/employee`, solo PROPIETARIO) | Hecha |
| 11 | Realizar pedido (`POST /api/v1/order`, solo CLIENTE) | Hecha |
| 10 | Listar platos de un restaurante (`GET /api/v1/dish?restaurantId=`, solo CLIENTE) | Hecha |
| 9 | Listar restaurantes (`GET /api/v1/restaurant`, solo CLIENTE) | Hecha |
| 7 | Habilitar/deshabilitar plato (`PATCH /api/v1/dish/{id}/active`, solo PROPIETARIO del restaurante) | Hecha |
| 4 | Modificar plato (`PATCH /api/v1/dish/{id}`, solo PROPIETARIO del restaurante) | Hecha |

## Configuración (variables de entorno)
Ninguna clave está escrita en el código. Antes de ejecutar define:

| Variable | Para qué | Obligatoria |
|----------|----------|-------------|
| `DB_PASSWORD` | Clave del usuario de MySQL | Sí |
| `DB_USERNAME` | Usuario de MySQL (por defecto `root`) | No |
| `JWT_SECRET` | Misma clave que usuarios-ms | Sí |

En Windows: copia `local-env.bat.example` como `local-env.bat`, pon tus valores y ejecuta `call local-env.bat`
en la misma consola antes de `gradlew.bat bootRun` (ese archivo está en `.gitignore`).
En Linux/macOS usa `export NOMBRE=valor`. `JWT_SECRET` debe ser **igual en los 4 microservicios** (mínimo 32 caracteres).

## Requisitos para ejecutar
1. `usuarios-ms` corriendo en `http://localhost:8081` (ver `services.usuarios.base-url`).
2. Mismo `JWT_SECRET` que `usuarios-ms`.
3. MySQL con la base `plazoleta`: `CREATE DATABASE plazoleta;`
4. `./gradlew bootRun` → http://localhost:8082/swagger-ui/index.html · Tests: `./gradlew test`

## Probar la HU2
1. En usuarios-ms: `POST /api/v1/auth/login` como admin y copia el token.
2. En usuarios-ms: crea un propietario (`POST /api/v1/user/owner`) y anota su id (`GET /api/v1/user/{id}` para verificar su rol).
3. Aquí, con `Authorization: Bearer <token>`:
```json
POST /api/v1/restaurant
{
  "name": "Sabor Criollo 54", "nit": "900123456", "address": "Calle 10 # 5-20",
  "phone": "+573005698325", "logoUrl": "https://miplazoleta.com/logo.png", "ownerId": 2
}
```
Respuestas: `201` creado · `400` dato inválido / id que no es propietario · `401` sin sesión ·
`403` no es admin · `409` NIT repetido · `503` usuarios-ms no responde.

## Probar la HU3
1. Entra como **propietario** (login en usuarios-ms con el correo y clave del propietario creado en la HU1) y copia su token.
2. Con ese token:
```json
POST /api/v1/dish
{
  "name": "Bandeja paisa", "price": 32000,
  "description": "Frijoles, arroz, carne molida, chicharrón, huevo y arepa",
  "imageUrl": "https://miplazoleta.com/platos/bandeja.jpg",
  "categoryId": 2, "restaurantId": 1
}
```
Categorías creadas al arrancar: 1 Entradas · 2 Platos fuertes · 3 Postres · 4 Bebidas · 5 Acompañamientos.

Respuestas: `201` creado (queda `active = true`) · `400` dato inválido / precio no entero o ≤ 0 / categoría inexistente ·
`401` sin sesión · `403` no es propietario o el restaurante es de otro · `404` restaurante inexistente.

## Probar la HU4
Con el token del **propietario** del restaurante al que pertenece el plato:
```json
PATCH /api/v1/dish/1
{ "price": 35000, "description": "Ahora con aguacate" }
```
Puedes enviar solo uno de los dos campos. Cualquier otro campo (por ejemplo `name`) se rechaza.

Respuestas: `200` modificado · `400` precio no entero o ≤ 0 / descripción vacía / sin campos / campos no permitidos ·
`401` sin sesión · `403` no es propietario o el plato es de otro restaurante · `404` plato inexistente.

## Probar la HU7
Con el token del **propietario** del restaurante al que pertenece el plato:
```json
PATCH /api/v1/dish/1/active
{ "active": false }
```
`false` deshabilita el plato y `true` lo habilita de nuevo.

Respuestas: `200` actualizado · `400` falta `active`, no es true/false o trae otros campos ·
`401` sin sesión · `403` no es propietario o el plato es de otro restaurante · `404` plato inexistente.

## Probar la HU9
Con el token de un **cliente** (se registra en usuarios-ms con `POST /api/v1/user/client`):
```
GET /api/v1/restaurant?page=0&size=10
```
Devuelve los restaurantes por orden alfabético, solo con `name` y `logoUrl`:
```json
{ "content": [ { "name": "Sabor Criollo 54", "logoUrl": "https://..." } ],
  "page": 0, "size": 10, "totalElements": 1, "totalPages": 1 }
```
`page` empieza en 0 (por defecto 0) y `size` por defecto es 10 (máximo 100).
Respuestas: `200` · `400` page negativo o size fuera de 1 a 100 · `401` sin sesión · `403` no es cliente.

## Probar la HU10
Con el token de un **cliente**:
```
GET /api/v1/dish?restaurantId=1&page=0&size=10
GET /api/v1/dish?restaurantId=1&categoryId=2&page=0&size=10
```
Devuelve solo los platos **activos** del restaurante, ordenados por categoría y nombre
(`id`, `name`, `price`, `description`, `imageUrl`, `categoryId`). `categoryId` es opcional.
Respuestas: `200` · `400` falta restaurantId, page negativo, size fuera de 1 a 100 o categoría inexistente ·
`401` sin sesión · `403` no es cliente · `404` el restaurante no existe.

## Probar la HU11
Con el token de un **cliente**:
```
POST /api/v1/order
{ "restaurantId": 1, "dishes": [ { "dishId": 1, "quantity": 2 }, { "dishId": 2, "quantity": 1 } ] }
```
Crea el pedido en estado `PENDIENTE` (tablas `orders` y `order_dishes`, se crean solas).
Respuestas: `201` con `id`, `status`, `date` y los platos · `400` datos inválidos, plato de otro restaurante o
inactivo, plato repetido · `401` sin sesión · `403` no es cliente · `404` restaurante o plato inexistente ·
`409` el cliente ya tiene un pedido `PENDIENTE`, `EN_PREPARACION` o `LISTO`.

## Probar la HU12
Primero el propietario vincula al empleado con su restaurante (se guarda en `restaurant_employees`):
```
POST /api/v1/restaurant/1/employee      (token del PROPIETARIO)
{ "employeeId": 3 }
```
Respuestas: `201` · `400` el usuario no existe o no es empleado · `403` no es propietario de ese restaurante ·
`404` restaurante inexistente · `409` el empleado ya está vinculado.

Luego el empleado lista los pedidos de su restaurante por estado:
```
GET /api/v1/order?status=PENDIENTE&page=0&size=10      (token del EMPLEADO)
```
Estados: `PENDIENTE`, `EN_PREPARACION`, `LISTO`, `ENTREGADO`, `CANCELADO`. Orden: del más antiguo al más reciente.
Respuestas: `200` con `id`, `clientId`, `status`, `date`, `chefId`, `dishes` · `400` estado inválido o ausente,
page negativo, size fuera de 1 a 100 · `401` sin sesión · `403` no es empleado o no está vinculado a un restaurante.

## Probar la HU13
Con el token de un **empleado** vinculado a un restaurante (HU12):
```
PATCH /api/v1/order/{id}/assign        (sin cuerpo)
```
El pedido pasa de `PENDIENTE` a `EN_PREPARACION` y queda con el empleado en `chefId`.
Respuestas: `200` con el pedido · `401` sin sesión · `403` no es empleado, no está vinculado a un restaurante
o el pedido es de otro restaurante · `404` el pedido no existe · `409` el pedido ya no está `PENDIENTE`.

## Probar la HU14
Requiere **mensajeria-ms** corriendo en el puerto 8083 (por defecto no envía SMS reales: escribe el mensaje en su consola).
Con el token de un **empleado** del restaurante, sobre un pedido `EN_PREPARACION` (HU13):
```
PATCH /api/v1/order/{id}/ready        (sin cuerpo)
```
El pedido pasa a `LISTO`, se genera un PIN de 6 dígitos (columna `security_pin` de `orders`, no se devuelve en la respuesta)
y se envía al celular del cliente. Si el mensaje no se puede enviar, el pedido **no** cambia de estado.
Respuestas: `200` · `400` el cliente no tiene celular o no está en formato internacional (`+573001112233`) ·
`401` sin sesión · `403` no es empleado, no está vinculado o el pedido es de otro restaurante · `404` pedido inexistente ·
`409` el pedido no está `EN_PREPARACION` · `503` mensajeria-ms no responde.

## Probar la HU15
Con el token de un **empleado** del restaurante, sobre un pedido `LISTO` (HU14), con el PIN que recibió el cliente:
```
PATCH /api/v1/order/{id}/deliver
{ "securityPin": "048213" }
```
Si el PIN coincide, el pedido pasa a `ENTREGADO`.
Respuestas: `200` · `400` PIN ausente o incorrecto (el pedido no cambia) · `401` sin sesión ·
`403` no es empleado, no está vinculado o el pedido es de otro restaurante · `404` pedido inexistente ·
`409` el pedido no está `LISTO`.

## Probar la HU16
Con el token del **cliente** dueño del pedido:
```
PATCH /api/v1/order/{id}/cancel        (sin cuerpo)
```
Solo se puede cancelar mientras el pedido esté `PENDIENTE`; pasa a `CANCELADO`.
Si ya está en preparación responde `409` con: *"Lo sentimos, tu pedido ya está en preparación y no puede cancelarse"*.
Respuestas: `200` · `401` sin sesión · `403` no es cliente o el pedido es de otro cliente · `404` pedido inexistente ·
`409` el pedido ya no está `PENDIENTE`.

## Probar la HU17
Requiere **trazabilidad-ms** (puerto 8084) y MongoDB corriendo, además de usuarios-ms y mensajeria-ms.
Cada cambio de estado de un pedido (crear, tomar, listo, entregar, cancelar) se registra solo en trazabilidad-ms.
Si trazabilidad-ms no responde, la operación falla con `503` y el pedido **no** cambia (no quedan pedidos sin historial).
El cliente consulta el historial en trazabilidad-ms: `GET /api/v1/trace/order/{orderId}` (ver su README).

## HU18 (eficiencia)
Los registros de trazabilidad ahora incluyen el restaurante y su propietario. La consulta de tiempos por pedido y el
ranking de empleados la ofrece `trazabilidad-ms` (ver su README): `GET /api/v1/trace/efficiency/orders` y
`GET /api/v1/trace/efficiency/ranking`, solo con token de PROPIETARIO.

# UCEShop — Microservicios con Spring Boot + PostgreSQL (Docker Compose)

UCEShop es un caso práctico de e-commerce **cloud-native** implementado con **dos microservicios Spring Boot** y una base de datos **PostgreSQL**, orquestado con **Docker Compose**. El proyecto separa responsabilidades en un servicio de catálogo y un servicio de órdenes, con comunicación **HTTP** entre microservicios y persistencia en PostgreSQL.

---

## Arquitectura

**Componentes**
- **catalog-service (Service A)**  
  Expone el catálogo de productos y permite consultar los ítems disponibles.
- **order-service (Service B)**  
  Permite crear órdenes y **valida el `itemId` consultando al `catalog-service` por HTTP** antes de persistir.
- **PostgreSQL (db)**  
  Base de datos para el caso (tablas `items` y `orders`), inicializada con un script SQL.
- **Docker Compose**  
  Orquestación de servicios, red interna, healthcheck y variables de entorno.

**Flujo principal (POST /orders)**
1. Cliente invoca `order-service` para crear una orden.
2. `order-service` valida campos (`itemId`, `quantity > 0`).
3. `order-service` consulta `catalog-service` por HTTP (`GET /catalog/items`) para verificar existencia del ítem.
4. Si existe, persiste en PostgreSQL (`orders`).
5. Retorna la orden creada.

---

## Tecnologías

- Java 17
- Spring Boot (Web, Data JPA, Actuator)
- PostgreSQL 16
- Maven
- Docker & Docker Compose
- Postman / curl (pruebas)

---

## Repositorio / Estructura

> La estructura puede variar levemente según tu organización, pero típicamente:

# UCEShop — Microservicios con Spring Boot + PostgreSQL (Docker Compose)

UCEShop es un caso práctico de e-commerce **cloud-native** implementado con **dos microservicios Spring Boot** y una base de datos **PostgreSQL**, orquestado con **Docker Compose**. El proyecto separa responsabilidades en un servicio de catálogo y un servicio de órdenes, con comunicación **HTTP** entre microservicios y persistencia en PostgreSQL.

---

## Arquitectura

**Componentes**
- **catalog-service (Service A)**  
  Expone el catálogo de productos y permite consultar los ítems disponibles.
- **order-service (Service B)**  
  Permite crear órdenes y **valida el `itemId` consultando al `catalog-service` por HTTP** antes de persistir.
- **PostgreSQL (db)**  
  Base de datos para el caso (tablas `items` y `orders`), inicializada con un script SQL.
- **Docker Compose**  
  Orquestación de servicios, red interna, healthcheck y variables de entorno.

**Flujo principal (POST /orders)**
1. Cliente invoca `order-service` para crear una orden.
2. `order-service` valida campos (`itemId`, `quantity > 0`).
3. `order-service` consulta `catalog-service` por HTTP (`GET /catalog/items`) para verificar existencia del ítem.
4. Si existe, persiste en PostgreSQL (`orders`).
5. Retorna la orden creada.

---

## Tecnologías

- Java 17
- Spring Boot (Web, Data JPA, Actuator)
- PostgreSQL 16
- Maven
- Docker & Docker Compose
- Postman / curl (pruebas)

---
## Base de datos (init.sql)

El proyecto inicializa las tablas y carga datos semilla del catálogo:

- `items(id, name, price)`
- `orders(id, item_id, quantity, created_at)`

**Nota de idempotencia:**  
La inserción de `items` se realiza con `ON CONFLICT (id) DO NOTHING`, lo que permite ejecutar el script múltiples veces sin duplicar los registros con los mismos IDs. Las sentencias `CREATE TABLE IF NOT EXISTS` también son idempotentes.

---

## Variables de entorno

Se utilizan variables para configurar puertos y conexión a la base, y para definir la URL base del catálogo consumida por `order-service`.

Ejemplos típicos:
- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `CATALOG_BASE_URL`

> En Docker Compose, el `order-service` debe apuntar a `http://catalog-service:8080` (hostname interno de la red de Compose), **no** a `localhost`.

---

## Endpoints

### catalog-service (Service A)
- **Health**
  - `GET /actuator/health`
- **Catálogo**
  - `GET /catalog/items`
  - Respuesta (ejemplo):
    ```json
    [
      {"id":1,"name":"Laptop Gamer","price":1299.99},
      {"id":2,"name":"Mouse Pro","price":49.90},
      {"id":3,"name":"Teclado Mecánico","price":89.50}
    ]
    ```

### order-service (Service B)
- **Health**
  - `GET /actuator/health`
- **Crear orden**
  - `POST /orders`
  - Body:
    ```json
    { "itemId": 1, "quantity": 2 }
    ```
  - Respuesta (ejemplo):
    ```json
    {
      "id": 1,
      "itemId": 1,
      "quantity": 2,
      "createdAt": "2026-02-06T18:13:51.566036427Z"
    }
    ```

> Si se intenta crear una orden con `itemId` inexistente, `order-service` responde `400` con un mensaje de validación.

---

## Actuator (observabilidad básica)

`/actuator/health` es un endpoint estándar de Spring Boot Actuator que reporta el estado del servicio.  
Se utiliza para:
- Verificar que el servicio está **UP**
- Habilitar **healthchecks** en contenedores
- Diagnóstico rápido en despliegues

En este proyecto se exponen los endpoints:
- `health`
- `info`

---

## Comunicación HTTP entre microservicios (order-service → catalog-service)

El `order-service` implementa un cliente HTTP (`CatalogClient`) que consume el endpoint del catálogo para validar existencia del ítem antes de persistir la orden.

- URL base configurable por propiedad/variable:
  - `catalog.base-url` (por ejemplo: `http://catalog-service:8080`)
- Llamada:
  - `GET {catalog.base-url}/catalog/items`
- Validación:
  - Se itera la lista retornada y se verifica coincidencia con `itemId`.

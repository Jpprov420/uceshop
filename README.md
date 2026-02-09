## Repositorio / Estructura (referencial)

# UCEShop — Microservicios Spring Boot + PostgreSQL (Docker Compose)

UCEShop es un caso práctico de e-commerce **cloud-native** implementado con **dos microservicios Spring Boot** y una base de datos **PostgreSQL**, orquestado con **Docker Compose**. El proyecto separa responsabilidades en un servicio de catálogo (productos/stock) y un servicio de órdenes (creación/consulta de órdenes), con comunicación **HTTP** entre microservicios y persistencia en PostgreSQL.

---

## Arquitectura

### Componentes
- **catalog-service (Service A)**  
  Gestión de catálogo (**items**) y stock (**quantity**). Expone endpoints REST para consultar y administrar productos.
- **order-service (Service B)**  
  Gestión de órdenes (**orders**). Para crear una orden, valida el `itemId` y **reserva stock consultando al `catalog-service` por HTTP** antes de persistir.
- **PostgreSQL (db)**  
  Base de datos del caso (tablas `items` y `orders`) inicializada con un script SQL idempotente.
- **Docker Compose**  
  Orquesta servicios, define una **red bridge común**, configura `healthcheck` para DB, aplica `depends_on` con `condition: service_healthy`, y usa variables desde `.env`.

### Flujo principal (POST /orders)
1. Cliente invoca `order-service` para crear una orden.
2. `order-service` valida body (`itemId`, `quantity > 0`).
3. `order-service` solicita reserva de stock a `catalog-service` por HTTP:
   - `POST /catalog/items/{id}/reserve?qty=X`
4. Si la reserva es exitosa, `order-service` persiste la orden en PostgreSQL (`orders`).
5. Retorna la orden creada.

**Nota clave:** el `order-service` **no** accede directamente a la tabla `items`; el control de stock se realiza a través de `catalog-service` (principio de separación de responsabilidades).

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

## Repositorio / Estructura (referencial)

.
├── docker-compose.yml
├── .env
├── db
│ └── 01_schema_seed.sql
├── catalog-service
│ ├── Dockerfile
│ └── src/main/java/... (controllers, entities, repositories)
└── order-service
├── Dockerfile
└── src/main/java/... (controllers, entities, repositories, integration)


---

## Requisitos para desplegar en otro equipo

### Opción recomendada (solo Docker)
Instalar:
- **Docker Desktop** (Windows/Mac) o **Docker Engine** (Linux)
- Docker Compose v2 (incluido en Docker Desktop)

> En Windows, puede requerirse WSL2 habilitado para ejecutar Docker Desktop con backend Linux.

### Opción adicional (si vas a compilar localmente)
- **JDK 17**
- **Maven**

---

## Instalación / Ejecución

### 1) Clonar repositorio

git clone <URL_DEL_REPO>
cd ucshop

### 2) Configurar .env

Verifica/ajusta variables (ejemplo típico):

POSTGRES_DB=uceshop
POSTGRES_USER=uceshop_user
POSTGRES_PASSWORD=uceshop_pass

CATALOG_PORT=8081
ORDER_PORT=8082

CATALOG_BASE_URL=http://catalog-service:8080

### 3) Levantar con Docker Compose
docker compose up -d --build

### 4) Ver estado
docker ps
docker compose logs -f

---

## Base de datos (init SQL) e idempotencia

El contenedor db ejecuta un script SQL desde:

/docker-entrypoint-initdb.d/01_schema_seed.sql

Incluye:

- Creación de tablas items y orders.

- Columna items.quantity.

-Seed inicial de productos.

## Idempotencia:

CREATE TABLE IF NOT EXISTS evita fallos si ya existen tablas.

ALTER TABLE ... ADD COLUMN IF NOT EXISTS evita fallos si la columna ya existe.

Para seeds, se usa ON CONFLICT (...) DO NOTHING o DO UPDATE según el escenario.

---

## Endpoints disponibles

Host local:

- catalog-service: http://localhost:8081

- order-service: http://localhost:8082

## catalog-service (Service A)

### Health

GET /actuator/health

## Items (CRUD)

GET /catalog/items

GET /catalog/items/{id}

POST /catalog/items

PUT /catalog/items/{id}

DELETE /catalog/items/{id}

### Stock

POST /catalog/items/{id}/reserve?qty=X
(descuenta stock si hay disponibilidad)

POST /catalog/items/{id}/release?qty=X
(agrega stock)

Regla anti-duplicados (POST items):

Si name ya existe → 409 Conflict

---

## order-service (Service B)

### Health

GET /actuator/health

### Órdenes

GET /orders
(lista y enriquece con info de item desde catalog-service)

GET /orders/{id}
(consulta por ID, con enriquecimiento)

POST /orders
(crea orden y reserva stock vía catalog-service)

---

## Conclusiones técnicas

Se implementó una arquitectura de microservicios con separación clara de responsabilidades:

- catalog-service como autoridad del catálogo/stock.

- order-service como autoridad de órdenes.

La comunicación HTTP interna (order-service → catalog-service) demuestra integración entre servicios sin acoplarlos a la base de datos del otro dominio.

La orquestación con Docker Compose (red bridge, healthchecks, depends_on, volumen persistente y variables .env) garantiza reproducibilidad, portabilidad y arranque controlado.

El diseño permite extender el sistema (más endpoints, validaciones, reglas de negocio, observabilidad) manteniendo una base consistente y desplegable en cualquier equipo con Docker.

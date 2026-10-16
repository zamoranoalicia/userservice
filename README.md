# UserService

UserService is a Spring Boot microservice that provides user registration and JWT-based authentication. This README documents how to build, run, configure, and test the service, includes application.yml details, and gives example requests for the main endpoints.

Prerequisites
- Java 21 (project configured to use the Java toolchain in build.gradle)
- Gradle wrapper (./gradlew) or a local Gradle installation

Build & Run
- Run in development with the Gradle wrapper:
  ./gradlew bootRun

- Build and run the JAR:
  ./gradlew build
  java -jar build/libs/*-0.0.1-SNAPSHOT.jar

- Run tests:
  ./gradlew test

Configuration (application.yml)
The main configuration file is at src/main/resources/application.yml. Important sections and examples:

spring:
  application:
    name: UserService

  datasource:
    url: jdbc:h2:mem:usersdb            # Default: in-memory H2
    driver-class-name: org.h2.Driver
    username: sa
    password: ""

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: validate                # Hibernate validates schema at startup
    show-sql: true

  h2:
    console:
      enabled: true                     # Enables H2 web console at /h2-console

  flyway:
    enabled: true
    locations: classpath:db/migration  # Flyway migrations live here and run on startup

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

security:
  jwt:
    secret: <base64-or-random-secret>  # Replace in production
    accessMinutes: 15
    refreshDays: 7

openapi:
  server-url: ${OPENAPI_SERVER_URL:http://localhost:8080}

config:
  user:
    domain:
      value: 'dominio.cl'
      errorMessage: "Email must belong to 'dominio.cl' domain."
  password:
    regex: ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$
    errorMessage: |
      Invalid password. Requirements:
      - At least one lowercase letter
      - At least one uppercase letter
      - At least one digit
      - At least one special character (@$!%*?&)
      - Minimum length 8 characters

Notes about configuration
- Flyway runs before Spring Boot fully starts. Because Hibernate's ddl-auto is set to `validate`, Flyway must create the schema that matches JPA entities.
- Override configuration using environment variables or an external config for production (e.g., SPRING_DATASOURCE_URL).

Database handling
- Default: in-memory H2 (jdbc:h2:mem:usersdb). Data is ephemeral and resets on app restart.
- Schema versioning: Flyway SQL migrations located in src/main/resources/db/migration create and evolve the schema.
- To use a persistent DB (recommended for production), update spring.datasource.url and credentials. Example Postgres config:
  spring.datasource.url: jdbc:postgresql://db-host:5432/usersdb
  spring.datasource.username: user
  spring.datasource.password: pass
  spring.jpa.database-platform: org.hibernate.dialect.PostgreSQLDialect

API Endpoints (examples)
1) Create user
- POST /user/create
- URL: http://localhost:8080/user/create
- Request JSON example:
  {
    "firstName": "Alicia",
    "lastName": "Zamorano",
    "email": "user@dominio.cl",
    "password": "Str0ng@Pass"
  }
- curl example:
  curl -s -X POST http://localhost:8080/user/create \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Alicia","lastName":"Zamorano","email":"user@dominio.cl","password":"Str0ng@Pass"}'

2) Login / authenticate
- POST /auth/login
- URL: http://localhost:8080/auth/login
- Request JSON example:
  { "email": "user@dominio.cl", "password": "Str0ng@Pass" }
- curl example:
  curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"user@dominio.cl","password":"Str0ng@Pass"}'
- Successful response: JSON with access token and refresh token (JWTs)

OpenAPI / Swagger
- API docs: http://localhost:8080/api-docs
- Swagger UI: http://localhost:8080/swagger-ui.html

H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:usersdb
- User: sa, password: (empty)

Security & secrets
- The JWT secret in application.yml is for development only. Use environment variables or secret management for production.
- Token lifetimes controlled by security.jwt.accessMinutes and security.jwt.refreshDays.

Testing
- Unit and integration tests can be run with:
  ./gradlew test

Project layout
- Controllers: src/main/java/org/azamorano/userService/presentation
- DTOs: src/main/java/org/azamorano/userService/presentation/dto
- Services and domain logic: src/main/java/org/azamorano/userService/application
- Flyway migrations: src/main/resources/db/migration
- Application configuration: src/main/resources/application.yml

---

Español

UserService es un microservicio Spring Boot que proporciona registro de usuarios y autenticación basada en JWT. A continuación se incluye una versión resumida en español del contenido anterior.

Requisitos
- Java 21
- Gradle wrapper (./gradlew) o Gradle local

Construcción y ejecución
- Ejecutar en desarrollo:
  ./gradlew bootRun
- Construir y ejecutar JAR:
  ./gradlew build
  java -jar build/libs/*-0.0.1-SNAPSHOT.jar
- Ejecutar pruebas:
  ./gradlew test

Configuración (application.yml)
El archivo principal está en src/main/resources/application.yml. Secciones importantes:
- spring.datasource.url: jdbc:h2:mem:usersdb (H2 en memoria por defecto)
- spring.jpa.hibernate.ddl-auto: validate (Hibernate valida el esquema al arrancar)
- spring.flyway.locations: classpath:db/migration (migraciones SQL de Flyway)
- security.jwt.secret: key usado para firmar JWTs (no guardar en repositorios)
- config.user.domain y config.password.* contienen validaciones y mensajes personalizados

Manejo de la base de datos
- Por defecto se usa H2 en memoria (datos efímeros).
- Flyway aplica migraciones SQL en src/main/resources/db/migration antes de la validación de Hibernate.
- Para producción, configurar una BD persistente (Postgres, MySQL) cambiando spring.datasource.url y credenciales.

Endpoints (ejemplos)
1) Crear usuario
- POST /user/create
- Ejemplo curl:
  curl -s -X POST http://localhost:8080/user/create \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Alicia","lastName":"Zamorano","email":"user@dominio.cl","password":"Str0ng@Pass"}'

2) Login / Autenticación
- POST /auth/login
- Ejemplo curl:
  curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"user@dominio.cl","password":"Str0ng@Pass"}'

Swagger / OpenAPI
- API docs: http://localhost:8080/api-docs
- Swagger UI: http://localhost:8080/swagger-ui.html

Consola H2
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:usersdb
- Usuario: sa (sin contraseña)

Seguridad y Keys
- Mantener los keys JWT fuera del control de versiones. Usar variables de entorno o un gestor de keys en producción.

Notas y buenas prácticas
- Usar una base de datos persistente en producción y configurar pooling.
- Asegurarse de que las migraciones Flyway creen el esquema esperado cuando Hibernate está en modo `validate`.

Estructura del proyecto
- Controllers: src/main/java/org/azamorano/userService/presentation
- DTOs: src/main/java/org/azamorano/userService/presentation/dto
- Servicios: src/main/java/org/azamorano/userService/application
- Migraciones Flyway: src/main/resources/db/migration
- Configuración: src/main/resources/application.yml


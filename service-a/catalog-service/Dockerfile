# ---- Build stage ----
FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Usuario no-root
RUN useradd -r -u 10001 appuser
USER appuser

# Copiamos el jar generado
COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

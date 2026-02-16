# ===============================
# FASE 1: CONSTRUCCIÓN (BUILD)
# ===============================
# Usamos una imagen que ya tiene Maven y Java 21 instalados
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Creamos carpeta de trabajo
WORKDIR /app

# Copiamos solo el archivo pom.xml y descargamos las librerías
# (Esto optimiza la velocidad de las siguientes construcciones)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos todo el código fuente del proyecto
COPY src ./src

# Compilamos y empaquetamos (Saltamos los tests para ir más rápido en el deploy)
RUN mvn clean package -DskipTests

# ===============================
# FASE 2: EJECUCIÓN (RUN)
# ===============================
# Usamos una imagen ligera de Java 21 para correr la app
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copiamos el .jar generado en la FASE 1 a esta nueva imagen
# Fíjate en el "--from=build"
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto
EXPOSE 8080

# Arrancamos la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
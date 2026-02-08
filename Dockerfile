# 1. Usamos una imagen base con Java 21 (la versión que usas)
FROM eclipse-temurin:21-jdk

# 2. Creamos una carpeta de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiamos el archivo .jar generado por Maven al contenedor
# (El asterisco * es para que no importe la versión 0.0.1 o 1.0.0)
COPY target/*.jar app.jar

# 4. Exponemos el puerto 8080 (donde escucha Spring Boot)
EXPOSE 8080

# 5. Comando para arrancar la app cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]
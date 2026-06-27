⚓ Battleship (Hundir la Flota) - API & Web App

Descripción:
Este proyecto es la implementación del clásico juego de mesa "Battleship" (Hundir la flota) donde un jugador se enfrenta contra la CPU.

El objetivo principal del ejercicio ha sido desarrollar una API RESTful robusta que gestione toda la lógica del juego (colocación de barcos, validación de disparos, cambios de turno y determinación del ganador), acompañada de un Frontend web interactivo y completamente modularizado. El proyecto incluye un sistema de autenticación para proteger las partidas en curso y un ranking global de las mejores puntuaciones.

Tecnologías Utilizadas:
El proyecto se ha desarrollado siguiendo una arquitectura cliente-servidor clásica, separando claramente las responsabilidades:

Backend:
Java: Lenguaje principal de la aplicación.
Spring Boot (v3.x): Framework principal para la creación del API REST.
Spring Security & JWT (JSON Web Tokens): Seguridad y autenticación sin estado (stateless) para proteger los endpoints del juego.
Swagger / OpenAPI 3: Documentación automática e interactiva de la API.

Frontend:
HTML5 & CSS3: Maquetación responsiva, uso de Variables CSS (:root), Flexbox y Grid. Estilos separados en múltiples módulos (base, layout, componentes, board, modales).
JavaScript (Vanilla JS): Lógica del cliente implementada con Módulos ES6 (import/export) dividida en responsabilidades:
api.js: Comunicación asíncrona (Fetch API) con el servidor.
ui.js: Manipulación del DOM y renderizado.
audio.js: Gestión de efectos de sonido y música de fondo.
state.js: Almacenamiento del estado local y sesión.
setup.js: Gestión de eventos para la colocación de la flota.
main.js: Controlador principal ("Director de orquesta").
Canvas Confetti: Librería externa para efectos visuales de celebración.

Requisitos:
Para poder compilar y ejecutar este proyecto en un entorno local, es necesario disponer del siguiente software instalado:

Java Development Kit (JDK): Versión 17 o superior.
Maven: Gestor de dependencias y construcción del proyecto.
Navegador Web Modern: (Chrome, Firefox, Safari, Edge) con soporte completo para ES6 Modules.
Instalación
Pasos para preparar el entorno de desarrollo:

Clonar el repositorio:
git clone <url-tu-repositorio>
cd battleship_api
Abrir el proyecto: Abre la carpeta con tu IDE de preferencia (IntelliJ IDEA, Eclipse, VS Code).**
Descargar dependencias: Deja que Maven descargue todas las librerías necesarias (o ejecuta mvn clean install desde la terminal).

Ejecución:
Inicia la aplicación Backend ejecutando la clase principal BattleshipApiApplication.java desde tu IDE, o mediante la terminal con el pedido:

Bash mvn spring-boot:run Una vez que el servidor arranque correctamente (verás el mensaje "Started BattleshipApiApplication"), abre tu navegador web.

Acceder al Juego: Navega en http://localhost:8080 para empezar a jugar.

Acceder a la API (Swagger): Navega en http://localhost:8080/swagger-ui/index.html para ver y probar la documentación interactiva de los endpoints.

Despliegue:
Para desplegar la aplicación en un entorno de producción (como un servidor VPS, AWS, Heroku o Render):

Genera el archivo ejecutable .jar mediante Maven:

Bash mvn clean package Esto generará un archivo dentro de la carpeta target/ (ej: battleship_api-0.0.1-SNAPSHOT.jar).

Transfiere este archivo a tu servidor y ejecútalo con Java:

Bash java -jar target/battleship_api-0.0.1-SNAPSHOT.jar Nota: Si se utiliza una base de datos externa en producción (ej: PostgreSQL en lugar de H2), será necesario configurar las variables de entorno correspondientes a la application.properties antes del despliegue.

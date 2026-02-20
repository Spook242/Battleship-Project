# ⚓ Battleship (Hundir la Flota) - API & Web App

## Descripció
Aquest projecte és la implementació del clàssic joc de taula "Battleship" (Hundir la flota) on un jugador s'enfronta contra la CPU.

L'objectiu principal de l'exercici ha estat desenvolupar una **API RESTful** robusta que gestioni tota la lògica del joc (col·locació de vaixells, validació de trets, canvis de torn i determinació del guanyador), acompanyada d'un **Frontend web interactiu** i completament modularitzat. El projecte inclou un sistema d'autenticació per protegir les partides en curs i un rànquing global de les millors puntuacions.

## Tecnologies Utilitzades
El projecte s'ha desenvolupat seguint una arquitectura client-servidor clàssica, separant clarament les responsabilitats:

**Backend:**
* **Java**: Llenguatge principal de l'aplicació.
* **Spring Boot (v3.x)**: Framework principal per a la creació de l'API REST.
* **Spring Security & JWT (JSON Web Tokens)**: Seguretat i autenticació sense estat (stateless) per a protegir els endpoints del joc.
* **Swagger / OpenAPI 3**: Documentació automàtica i interactiva de l'API.

**Frontend:**
* **HTML5 & CSS3**: Maquetació responsiva, ús de Variables CSS (`:root`), Flexbox i Grid. Estils separats en múltiples mòduls (base, layout, components, board, modals).
* **JavaScript (Vanilla JS)**: Lògica del client implementada amb **Mòduls ES6** (`import`/`export`) dividida en responsabilitats:
    * `api.js`: Comunicació asíncrona (Fetch API) amb el servidor.
    * `ui.js`: Manipulació del DOM i renderitzat.
    * `audio.js`: Gestió d'efectes de so i música de fons.
    * `state.js`: Emmagatzematge de l'estat local i sessió.
    * `setup.js`: Gestió d'esdeveniments per a la col·locació de la flota.
    * `main.js`: Controlador principal ("Director d'orquestra").
* **Canvas Confetti**: Llibreria externa per a efectes visuals de celebració.

## Requisits
Per poder compilar i executar aquest projecte en un entorn local, cal disposar del següent programari instal·lat:
* **Java Development Kit (JDK)**: Versió 17 o superior.
* **Maven**: Gestor de dependències i construcció del projecte.
* **Navegador Web Modern**: (Chrome, Firefox, Safari, Edge) amb suport complet per a *ES6 Modules*.

## Instal·lació
Passos per a preparar l'entorn de desenvolupament:

1. **Clonar el repositori:**
   ```bash
   git clone <url-del-teu-repositori>
   cd battleship_api

2. **Obrir el projecte:**
Obre la carpeta amb el teu IDE de preferència (IntelliJ IDEA, Eclipse, VS Code).**

**Descarregar dependències:** 
Deixa que Maven descarregui totes les llibreries necessàries (o executa mvn clean install des de la terminal).

## Execució
Inicia l'aplicació Backend executant la classe principal BattleshipApiApplication.java des del teu IDE, o mitjançant la terminal amb la comanda:

Bash
mvn spring-boot:run
Un cop el servidor arrenqui correctament (veuràs el missatge "Started BattleshipApiApplication"), obre el teu navegador web.

Accedir al Joc: Navega a http://localhost:8080 per començar a jugar.

Accedir a l'API (Swagger): Navega a http://localhost:8080/swagger-ui/index.html per veure i provar la documentació interactiva dels endpoints.

## Desplegament
Per a desplegar l'aplicació en un entorn de producció (com un servidor VPS, AWS, Heroku o Render):

Genera l'arxiu executable .jar mitjançant Maven:

Bash
mvn clean package
Això generarà un arxiu dins de la carpeta target/ (ex: battleship_api-0.0.1-SNAPSHOT.jar).

Transfereix aquest arxiu al teu servidor i executa'l amb Java:

Bash
java -jar target/battleship_api-0.0.1-SNAPSHOT.jar
Nota: Si s'utilitza una base de dades externa en producció (ex: PostgreSQL en lloc de H2), caldrà configurar les variables d'entorn corresponents a l'application.properties abans del desplegament.
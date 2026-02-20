// state.js

export const gameState = {
    // Datos generales de la partida
    gameId: null,
    isFinished: false,
    username: "",
    token: null,

    // Datos exclusivos de la fase de colocación (Setup)
    setup: {
        isActive: false,
        isHorizontal: true,
        shipsToPlace: [5, 4, 3, 3, 2],
        currentIndex: 0,
        myPlacedShips: []
    },

    // 1. Método para guardar la sesión cuando creamos partida
    saveSession(token, gameId, username) {
        this.token = token;
        this.gameId = gameId;
        this.username = username;
        localStorage.setItem("jwt_token", token);
    },

    // 2. Método para resetear todo (útil cuando el jugador le da a "Volver a jugar")
   // 2. Método para resetear todo
      // 2. Método para resetear la partida (Reseteo Suave - Mantiene el nombre)
          reset() {
              this.gameId = null;
              this.isFinished = false;
              this.token = null;
              // ⚠️ Fíjate que ya NO borramos this.username aquí

              // Reseteamos también el setup
              this.setup.isActive = false;
              this.setup.isHorizontal = true;
              this.setup.currentIndex = 0;
              this.setup.myPlacedShips = [];
              this.setup.shipsToPlace = [5, 4, 3, 3, 2];
          },

          // 2.5. Método para salir al menú (Reseteo Total - Borra el nombre)
          fullReset() {
              this.reset();
              this.username = ""; // Ahora SÍ borramos al capitán
          },

    // 3. Métodos de ayuda para la fase de Setup
    getCurrentShipSize() {
        return this.setup.shipsToPlace[this.setup.currentIndex];
    },

    isSetupComplete() {
        return this.setup.currentIndex >= this.setup.shipsToPlace.length;
    },

    addPlacedShip(ship) {
        this.setup.myPlacedShips.push(ship);
        this.setup.currentIndex++;
    }
};
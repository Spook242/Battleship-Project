export const gameState = {
    
    gameId: null,
    isFinished: false,
    username: "",
    token: null,

    setup: {
        isActive: false,
        isHorizontal: true,
        shipsToPlace: [5, 4, 3, 3, 2],
        currentIndex: 0,
        myPlacedShips: []
    },

    saveSession(token, gameId, username) {
        this.token = token;
        this.gameId = gameId;
        this.username = username;
        localStorage.setItem("jwt_token", token);
    },

     reset() {
              this.gameId = null;
              this.isFinished = false;
              this.token = null;

              this.setup.isActive = false;
              this.setup.isHorizontal = true;
              this.setup.currentIndex = 0;
              this.setup.myPlacedShips = [];
              this.setup.shipsToPlace = [5, 4, 3, 3, 2];
          },

          fullReset() {
              this.reset();
              this.username = ""; 
          },

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
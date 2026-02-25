import { gameState } from './state.js';
import { uiManager } from './ui.js';
import { audioManager } from './audio.js';

export const setupManager = {
    onSetupCompleteCallback: null,

    start(onComplete) {
        this.onSetupCompleteCallback = onComplete;

        gameState.setup.isActive = true;
        gameState.setup.currentIndex = 0;
        gameState.setup.myPlacedShips = [];
        gameState.setup.isHorizontal = true;
        gameState.setup.shipsToPlace = [5, 4, 3, 3, 2];

        uiManager.updateBoard("player-board", { ships: [], shotsReceived: [] }, false);
        uiManager.updateBoard("cpu-board", { ships: [], shotsReceived: [] }, true);
        uiManager.clearAlertPanels();
        uiManager.updateFleetStatusPanel("player-status-panel", []);
        uiManager.updateFleetStatusPanel("cpu-status-panel", []);

        document.getElementById("cpu-board").style.opacity = "0.5";
        uiManager.updateStatusText(
            "PLACE YOUR SHIPS! (Press 'R' to Rotate)",
            "Current Ship Size: " + gameState.getCurrentShipSize()
        );

        this.bindEvents();
    },

    bindEvents() {
        const cells = document.querySelectorAll("#player-board .cell");

        cells.forEach(cell => {
            cell.onmouseover = () => this.previewShip(cell, true);
            cell.onmouseout  = () => this.previewShip(cell, false);
            cell.onclick     = () => this.placeShip(cell);
            cell.oncontextmenu = (e) => { e.preventDefault(); this.rotateShip(); };
        });

        this.handleRotateKey = this.handleRotateKey.bind(this);
        document.addEventListener('keydown', this.handleRotateKey);
    },

    handleRotateKey(e) {
        if (e.key === 'r' || e.key === 'R') this.rotateShip();
    },

    rotateShip() {
        if (!gameState.setup.isActive) return;

        gameState.setup.isHorizontal = !gameState.setup.isHorizontal;

        const isHoriz = gameState.setup.isHorizontal;
        const size = gameState.getCurrentShipSize();

        uiManager.updateStatusText(
            "PLACE YOUR SHIPS! (Press 'R' to Rotate)",
            `Size: ${size} (${isHoriz ? "Horizontal ➡" : "Vertical ⬇"})`
        );
    },

    previewShip(cell, show) {
        if (!gameState.setup.isActive) return;

        const coord = cell.dataset.coord;
        const size = gameState.getCurrentShipSize();
        const cellsToPaint = this.getShipCoordinates(coord, size, gameState.setup.isHorizontal);

        document.querySelectorAll(".cell.preview-valid, .cell.preview-invalid").forEach(c => {
            c.classList.remove("preview-valid", "preview-invalid");
        });

        if (show) {
            const isValid = this.isValidPlacement(cellsToPaint);
            cellsToPaint.forEach(cCoord => {
                const cDiv = document.querySelector(`#player-board .cell[data-coord="${cCoord}"]`);
                if (cDiv) {
                    cDiv.classList.add(isValid ? "preview-valid" : "preview-invalid");
                }
            });
        }
    },

    placeShip(cell) {
        if (!gameState.setup.isActive) return;

        const coord = cell.dataset.coord;
        const size = gameState.getCurrentShipSize();
        const cellsToPaint = this.getShipCoordinates(coord, size, gameState.setup.isHorizontal);

        if (!this.isValidPlacement(cellsToPaint)) {
            audioManager.playError();
            return;
        }

        audioManager.playHammer();

        const newShip = {
            type: "Ship-" + size,
            size: size,
            cells: cellsToPaint,
            hits: [],
            sunk: false
        };

        
        gameState.addPlacedShip(newShip);

        
        cellsToPaint.forEach(cCoord => {
            const cDiv = document.querySelector(`#player-board .cell[data-coord="${cCoord}"]`);
            if (cDiv) {
                cDiv.classList.add("ship", "placed");
                cDiv.classList.remove("preview-valid", "preview-invalid");
            }
        });

        
        if (gameState.isSetupComplete()) {
            this.finishSetup();
        } else {
            uiManager.updateStatusText(
                "PLACE YOUR SHIPS! (Press 'R' to Rotate)",
                "Next Ship Size: " + gameState.getCurrentShipSize()
            );
        }
    },
    
    isValidPlacement(coords) {
        if (coords.length !== gameState.getCurrentShipSize()) return false;

        for (let c of coords) {
            for (let ship of gameState.setup.myPlacedShips) {
                if (ship.cells.includes(c)) return false; 
            }
        }
        return true;
    },

    getShipCoordinates(startCoord, size, horizontal) {
        const row = startCoord.charCodeAt(0);
        const col = parseInt(startCoord.substring(1));
        const coords = [];

        for (let i = 0; i < size; i++) {
            let r = horizontal ? row : row + i;
            let c = horizontal ? col + i : col;

            if (r > 74 || c > 10) break; 

            const newCoord = String.fromCharCode(r) + c;
            coords.push(newCoord);
        }
        return coords;
    },

    finishSetup() {
        gameState.setup.isActive = false;

        document.removeEventListener('keydown', this.handleRotateKey);

        uiManager.updateStatusText("DEPLOYING FLEET...", "PLEASE WAIT");

        
        if (this.onSetupCompleteCallback) {
            this.onSetupCompleteCallback(gameState.setup.myPlacedShips);
        }
    }
};
// setup.js
import { gameState } from './state.js';
import { uiManager } from './ui.js';
import { audioManager } from './audio.js';

export const setupManager = {
    // Aquí guardaremos la función que nos pase el main.js para cuando terminemos
    onSetupCompleteCallback: null,

    // ==========================================
    // 1. INICIAR LA FASE
    // ==========================================
    start(onComplete) {
        this.onSetupCompleteCallback = onComplete;

        // 1. Activamos el modo Setup en nuestro estado central
        gameState.setup.isActive = true;
        gameState.setup.currentIndex = 0;
        gameState.setup.myPlacedShips = [];
        gameState.setup.isHorizontal = true;
        gameState.setup.shipsToPlace = [5, 4, 3, 3, 2];

        // 2. Limpieza Visual Total usando el uiManager
        uiManager.updateBoard("player-board", { ships: [], shotsReceived: [] }, false);
        uiManager.updateBoard("cpu-board", { ships: [], shotsReceived: [] }, true);
        uiManager.clearAlertPanels();
        uiManager.updateFleetStatusPanel("player-status-panel", []);
        uiManager.updateFleetStatusPanel("cpu-status-panel", []);

        // 3. Configuración visual específica del setup
        document.getElementById("cpu-board").style.opacity = "0.5";
        uiManager.updateStatusText(
            "PLACE YOUR SHIPS! (Press 'R' to Rotate)",
            "Current Ship Size: " + gameState.getCurrentShipSize()
        );

        // 4. Activar los eventos del ratón y teclado
        this.bindEvents();
    },

    // ==========================================
    // 2. GESTIÓN DE EVENTOS (Ratón y Teclado)
    // ==========================================
    bindEvents() {
        const cells = document.querySelectorAll("#player-board .cell");

        cells.forEach(cell => {
            cell.onmouseover = () => this.previewShip(cell, true);
            cell.onmouseout  = () => this.previewShip(cell, false);
            cell.onclick     = () => this.placeShip(cell);
            // Click derecho para rotar
            cell.oncontextmenu = (e) => { e.preventDefault(); this.rotateShip(); };
        });

        // Evento de la tecla R
        this.handleRotateKey = this.handleRotateKey.bind(this);
        document.addEventListener('keydown', this.handleRotateKey);
    },

    handleRotateKey(e) {
        if (e.key === 'r' || e.key === 'R') this.rotateShip();
    },

    rotateShip() {
        if (!gameState.setup.isActive) return;

        // Invertimos la rotación en el estado
        gameState.setup.isHorizontal = !gameState.setup.isHorizontal;

        const isHoriz = gameState.setup.isHorizontal;
        const size = gameState.getCurrentShipSize();

        uiManager.updateStatusText(
            "PLACE YOUR SHIPS! (Press 'R' to Rotate)",
            `Size: ${size} (${isHoriz ? "Horizontal ➡" : "Vertical ⬇"})`
        );
    },

    // ==========================================
    // 3. PINTAR PREVISUALIZACIÓN Y COLOCAR
    // ==========================================
    previewShip(cell, show) {
        if (!gameState.setup.isActive) return;

        const coord = cell.dataset.coord;
        const size = gameState.getCurrentShipSize();
        const cellsToPaint = this.getShipCoordinates(coord, size, gameState.setup.isHorizontal);

        // Limpiar previsualizaciones anteriores
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

        // 1. SI ES INVÁLIDO ❌
        if (!this.isValidPlacement(cellsToPaint)) {
            audioManager.playError();
            return;
        }

        // 2. SI ES VÁLIDO ✅
        audioManager.playHammer();

        const newShip = {
            type: "Ship-" + size,
            size: size,
            cells: cellsToPaint,
            hits: [],
            sunk: false
        };

        // Guardamos en el state
        gameState.addPlacedShip(newShip);

        // Pintamos el barco fijo
        cellsToPaint.forEach(cCoord => {
            const cDiv = document.querySelector(`#player-board .cell[data-coord="${cCoord}"]`);
            if (cDiv) {
                cDiv.classList.add("ship", "placed");
                cDiv.classList.remove("preview-valid", "preview-invalid");
            }
        });

        // 3. ¿HEMOS TERMINADO?
        if (gameState.isSetupComplete()) {
            this.finishSetup();
        } else {
            uiManager.updateStatusText(
                "PLACE YOUR SHIPS! (Press 'R' to Rotate)",
                "Next Ship Size: " + gameState.getCurrentShipSize()
            );
        }
    },

    // ==========================================
    // 4. LÓGICA MATEMÁTICA (Coordenadas)
    // ==========================================
    isValidPlacement(coords) {
        if (coords.length !== gameState.getCurrentShipSize()) return false;

        for (let c of coords) {
            for (let ship of gameState.setup.myPlacedShips) {
                if (ship.cells.includes(c)) return false; // Choque con otro barco
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

            if (r > 74 || c > 10) break; // 74 es 'J' en ASCII. Evita salirse del tablero

            const newCoord = String.fromCharCode(r) + c;
            coords.push(newCoord);
        }
        return coords;
    },

    // ==========================================
    // 5. FINALIZAR FASE
    // ==========================================
    finishSetup() {
        gameState.setup.isActive = false;

        // Apagamos el evento del teclado para que no consuma memoria
        document.removeEventListener('keydown', this.handleRotateKey);

        uiManager.updateStatusText("DEPLOYING FLEET...", "PLEASE WAIT");

        // ¡Magia! Llamamos al director de orquesta (main.js) y le damos los barcos listos
        if (this.onSetupCompleteCallback) {
            this.onSetupCompleteCallback(gameState.setup.myPlacedShips);
        }
    }
};
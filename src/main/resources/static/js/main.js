// main.js
import { api } from './api.js';
import { audioManager } from './audio.js';
import { gameState } from './state.js';
import { uiManager } from './ui.js';
import { setupManager } from './setup.js';

// ==========================================
// 1. INICIAR PARTIDA
// ==========================================
// ==========================================
// 1. INICIAR PARTIDA
// ==========================================
async function createGame() {
    console.log("🟢 Iniciando partida...");
    uiManager.stopConfetti();
    audioManager.stopAllMusic();
    audioManager.playIntro();

    // El .trim() quita los espacios en blanco por si el usuario solo pone espacios
    const usernameInput = document.getElementById("username").value.trim() || gameState.username;

    // 1. Si no hay nombre, mostramos TU error rojo y cortamos la función
    if (!usernameInput) {
        uiManager.showLoginError("Please enter your captain's name ⚠️");
        return;
    }

    // 2. Si hay nombre, ocultamos el error (por si estaba visible de antes)
    uiManager.hideLoginError();

    try {
        const data = await api.createGame(usernameInput);

        // Guardar sesión
        gameState.saveSession(data.token, data.game.id, usernameInput);

        // Cambiar pantalla
        uiManager.showGamePanel();

        if (data.game.status === "SETUP") {
            setupManager.start(handleSetupComplete);
        } else {
            refreshGameScreen(data.game);
        }
    } catch (error) {
        console.error(error);
        uiManager.showLoginError("Error de conexión con el Cuartel General 📡");
    }
}

// ==========================================
// 2. CONECTAR SETUP CON BATALLA
// ==========================================
async function handleSetupComplete(placedShips) {
    try {
        const data = await api.startBattle(gameState.gameId, placedShips, gameState.token);

        document.getElementById("cpu-board").style.opacity = "1";
        refreshGameScreen(data);
    } catch (error) {
        console.error("Error al iniciar la batalla:", error);
    }
}

// ==========================================
// 3. TURNO DEL JUGADOR (Disparar)
// ==========================================
async function fire(coordinate) {
    if (gameState.isFinished) return;

    audioManager.playShot();

    try {
        const game = await api.fire(gameState.gameId, coordinate, gameState.token);
        refreshGameScreen(game);

        // --- Lógica Visual de Impacto ---
        let isHit = false;
        let isSunk = false;

        for (let ship of game.cpuBoard.ships) {
            if (ship.cells.includes(coordinate)) {
                isHit = true;
                if (ship.sunk) isSunk = true;
                break;
            }
        }

        uiManager.updateAlertPanel("player-alert-panel", coordinate, isHit, false);

        if (isHit) {
            audioManager.playBoom();
            uiManager.showExplosion(coordinate, "cpu-board");

            if (isSunk && game.status !== "FINISHED") {
                audioManager.playMayday();
                uiManager.showShotMessage(`${coordinate} HIT AND SUNK! ☠️`, "sunk");
            }
        } else {
            audioManager.playWater();
        }

        checkGameStatus(game);

    } catch (error) {
        console.error("Error disparando:", error);
    }
}

// ==========================================
// 4. TURNO DE LA CPU
// ==========================================
async function playCpuTurn() {
    if (gameState.isFinished) return;

    try {
        const game = await api.playCpuTurn(gameState.gameId, gameState.token);
        refreshGameScreen(game);

        // --- Lógica Visual de Impacto CPU ---
        const shots = game.playerBoard.shotsReceived;
        let sunkInThisTurn = false;

        if (shots.length > 0) {
            const lastShot = shots[shots.length - 1];
            let isHit = false;

            for (let ship of game.playerBoard.ships) {
                if (ship.cells.includes(lastShot)) {
                    isHit = true;
                    if (ship.sunk) sunkInThisTurn = true;
                    break;
                }
            }

            uiManager.updateAlertPanel("cpu-alert-panel", lastShot, isHit, true);

            if (isHit) {
                audioManager.playBoom();
                uiManager.showExplosion(lastShot, "player-board");

                if (sunkInThisTurn && game.status !== "FINISHED") {
                    audioManager.playMayday();
                    uiManager.showShotMessage(`CPU: ${lastShot} HIT AND SUNK! ☠️`, "sunk");
                }
            } else {
                audioManager.playWater();
            }
        }

        // Tiempos de espera para que se vea la jugada
        if (game.status === "FINISHED") {
            setTimeout(() => checkGameStatus(game), 250);
        } else if (sunkInThisTurn) {
            setTimeout(() => checkGameStatus(game), 1750);
        } else {
            checkGameStatus(game);
        }

    } catch (error) {
        console.error("Error CPU:", error);
    }
}

// ==========================================
// 5. CONTROLADORES GLOBALES
// ==========================================
function refreshGameScreen(game) {
    // FÍJATE AQUÍ: Le pasamos 'fire' al tablero enemigo para que sepa qué hacer al hacer clic
    uiManager.updateBoard("player-board", game.playerBoard, false);
    uiManager.updateBoard("cpu-board", game.cpuBoard, true, fire);

    uiManager.updateFleetStatusPanel("player-status-panel", game.playerBoard.ships);
    uiManager.updateFleetStatusPanel("cpu-status-panel", game.cpuBoard.ships);
}

function checkGameStatus(game) {
    if (game.status === "FINISHED") {
        gameState.isFinished = true;
        uiManager.updateStatusText("GAME OVER", "");
        audioManager.stopIntro();

        uiManager.showGameOverModal(game.winner);
        if (game.winner === "PLAYER") audioManager.playWin();
        else audioManager.playLose();

        return;
    }

    if (game.turn === "PLAYER") {
        uiManager.updateStatusText("WAITING FOR COORDINATES...", "PLAYER TURN... 🟢");
    } else {
        uiManager.updateStatusText("CALCULATING COORDINATES...", "CPU TURN... 🔴");
        setTimeout(playCpuTurn, 1250);
    }
}

// Botones del menú
async function showRanking() {
    uiManager.showRankingLoading();
    try {
        const ranking = await api.getRanking();
        uiManager.renderRankingList(ranking);
    } catch (error) {
        uiManager.showRankingError();
    }
}

function restartGame() {
    // 1. Reseteo Suave (Tu nombre sigue guardado en la memoria)
    gameState.reset();

    // 2. Paramos el vídeo y la música de victoria/derrota
    audioManager.stopAllMusic();
    uiManager.stopConfetti();
    uiManager.stopVideo();

    // 3. ¡Magia! Llamamos directamente a crear partida.
    // Como el nombre sigue en memoria, se saltará el menú y te llevará a colocar barcos.
    createGame();
}

function exitToMenu() {
    // 1. Reseteo Total (Destruye tu nombre de la memoria)
    gameState.fullReset();

    // 2. Apagamos todo
    audioManager.stopAllMusic();
    uiManager.stopConfetti();
    uiManager.stopVideo();

    // 3. Volvemos al menú principal visualmente
    uiManager.returnToMenu();

    // 4. Encendemos la música de la pantalla de inicio
    audioManager.playIntro();
}

// ==========================================
// EXPORTAR FUNCIONES AL HTML
// ==========================================
// Como usamos módulos, el HTML no ve estas funciones automáticamente.
// Tenemos que engancharlas al objeto global 'window'.
window.createGame = createGame;
window.restartGame = restartGame;
window.exitToMenu = exitToMenu;
window.showRanking = showRanking;
window.closeRanking = () => uiManager.closeRanking();
// ==========================================
// INICIALIZACIÓN AL CARGAR LA PÁGINA
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    // Intentamos reproducir la música de intro al cargar
    audioManager.playIntro();

    // Si el navegador la bloquea (política de Autoplay), la disparamos con el primer clic
    document.addEventListener('click', () => {
        const audio = document.getElementById("introAudio");
        if (audio && audio.paused) {
            audioManager.playIntro();
        }
    }, { once: true });
});
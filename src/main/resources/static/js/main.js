import { api } from './api.js';
import { audioManager } from './audio.js';
import { gameState } from './state.js';
import { uiManager } from './ui.js';
import { setupManager } from './setup.js';


async function loginUser() {
    const usernameInput = document.getElementById("username").value.trim();
    const passwordInput = document.getElementById("password").value.trim();

    if (!usernameInput || !passwordInput) {
        uiManager.showLoginError("⚠️ Please enter your nickname and password ⚠️");
        return;
    }

    uiManager.hideLoginError();

    try {
        const authResponse = await api.login(usernameInput, passwordInput);

        localStorage.setItem('jwt_token', authResponse.token);

        uiManager.showRadarAlert("LOGIN SUCCESSFUL", "Welcome back, Captain! Accessing command center...", 5000);
        setTimeout(() => {
            audioManager.playSonar();
               }, 0);

        createGame(usernameInput);
    } catch (error) {
        uiManager.showLoginError("⚠️ "+ error.message + " ⚠️");
    }
}

async function registerUser() {
    const usernameInput = document.getElementById("username").value.trim();
    const passwordInput = document.getElementById("password").value.trim();

    if (!usernameInput || !passwordInput) {
        uiManager.showLoginError("⚠️ Please enter your nickname and password ⚠️");
        return;
    }

    uiManager.hideLoginError();

    try {
        const authResponse = await api.register(usernameInput, passwordInput);

        localStorage.setItem('jwt_token', authResponse.token);
       uiManager.showRadarAlert("NEW CAPTAIN REGISTERED", "Welcome aboard! Entering command center...", 5000);
       setTimeout(() => {
                   audioManager.playSonar();
               }, 0);

        createGame(usernameInput);
    } catch (error) {
        uiManager.showLoginError("⚠️ " + error.message + " ⚠️");
    }
}

async function createGame(authenticatedUsername) {
    console.log("🟢 Iniciando partida...");
    uiManager.stopConfetti();
    audioManager.stopAllMusic();
    audioManager.playIntro();

    
    const usernameToUse = authenticatedUsername || gameState.username;

    try {
        
        const data = await api.createGame(usernameToUse);

        const token = localStorage.getItem('jwt_token');

        const gameId = data.game ? data.game.id : data.id;

        gameState.saveSession(token, gameId, usernameToUse);

        uiManager.showGamePanel();

        const status = data.game ? data.game.status : data.status;
        if (status === "SETUP") {
            setupManager.start(handleSetupComplete);
        } else {
            refreshGameScreen(data.game || data);
        }
    } catch (error) {
        console.error(error);
        uiManager.showLoginError("Connection error with Headquarters 📡");
    }
}

async function handleSetupComplete(placedShips) {
    try {
        const data = await api.startBattle(gameState.gameId, placedShips, gameState.token);

        document.getElementById("cpu-board").style.opacity = "1";
        refreshGameScreen(data);
    } catch (error) {
        console.error("Error al iniciar la batalla:", error);
    }
}

async function fire(coordinate) {
    if (gameState.isFinished) return;

    audioManager.playShot();

    try {
        const game = await api.fire(gameState.gameId, coordinate, gameState.token);
        refreshGameScreen(game);

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

async function playCpuTurn() {
    if (gameState.isFinished) return;

    try {
        const game = await api.playCpuTurn(gameState.gameId, gameState.token);
        refreshGameScreen(game);

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

function refreshGameScreen(game) {
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
    gameState.reset();

    audioManager.stopAllMusic();
    uiManager.stopConfetti();
    uiManager.stopVideo();

    createGame();
}

function exitToMenu() {
    gameState.fullReset();

    audioManager.stopAllMusic();
    uiManager.stopConfetti();
    uiManager.stopVideo();

    uiManager.returnToMenu();

    audioManager.playIntro();
}

window.createGame = createGame;
window.restartGame = restartGame;
window.exitToMenu = exitToMenu;
window.showRanking = showRanking;
window.closeRanking = () => uiManager.closeRanking();
window.loginUser = loginUser;
window.registerUser = registerUser;
document.addEventListener('DOMContentLoaded', () => {
    audioManager.playIntro();

    document.addEventListener('click', () => {
        const audio = document.getElementById("introAudio");
        if (audio && audio.paused) {
            audioManager.playIntro();
        }
    }, { once: true });
});
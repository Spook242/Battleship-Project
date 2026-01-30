const API_URL = "http://localhost:8080/game";
let currentGameId = null;
let gameFinished = false;
let currentUsername = "";

// SONIDOS
const soundShot = new Audio('sounds/shot.mp3');
const soundWater = new Audio('sounds/water.mp3');
const soundBoom = new Audio('sounds/boom.mp3');

// 1. CREAR PARTIDA
async function createGame() {
    const usernameInput = document.getElementById("username");
    const username = usernameInput.value || currentUsername;

    if (!username) {
        alert("¡Por favor, introduce tu nombre de Capitán!");
        return;
    }

    currentUsername = username;

    try {
        const response = await fetch(`${API_URL}/new`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: username })
        });

        if (response.ok) {
            const game = await response.json();
            currentGameId = game.id;
            gameFinished = false;

            // === UI: TRANSICIÓN ===
            document.getElementById("login-panel").style.display = "none";
            document.getElementById("full-screen-bg").style.display = "none";
            document.getElementById("game-title").style.display = "none";

            document.getElementById("game-panel").style.display = "block";
            document.getElementById("game-over-modal").style.display = "none";

            updateBoard("player-board", game.playerBoard, false);
            updateBoard("cpu-board", game.cpuBoard, true);
            updateStatus(game);
        } else {
            alert("Error al crear la partida");
        }
    } catch (error) {
        console.error("Error:", error);
        alert("No se pudo conectar con el servidor.");
    }
}

// 2. DISPARAR (TURNO JUGADOR)
async function fire(coordinate) {
    if (gameFinished) return;

    soundShot.play();

    try {
        const response = await fetch(`${API_URL}/${currentGameId}/fire`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ coordinate: coordinate })
        });

        if (response.ok) {
            const game = await response.json();

            // 1. Actualizamos tableros
            updateBoard("player-board", game.playerBoard, false);
            updateBoard("cpu-board", game.cpuBoard, true);
            updateStatus(game);

            // 2. Calculamos si acertamos
            let hit = false;
            for(let ship of game.cpuBoard.ships) {
                if(ship.cells.includes(coordinate)) {
                    hit = true; break;
                }
            }

            // 3. Efectos (Explosión en CPU-BOARD)
            if(hit) {
                soundBoom.play();
                showExplosion(coordinate, "cpu-board"); // <--- Pasamos el ID del tablero enemigo
            } else {
                soundWater.play();
            }
        }
    } catch (error) {
        console.error("Error disparando:", error);
    }
}

// 3. TURNO CPU (AHORA CON EXPLOSIONES)
async function playCpuTurn() {
    if (gameFinished) return;

    try {
        const response = await fetch(`${API_URL}/${currentGameId}/cpu-turn`, {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        });

        if (response.ok) {
            const game = await response.json();

            // 1. Actualizamos tableros
            updateBoard("player-board", game.playerBoard, false);
            updateBoard("cpu-board", game.cpuBoard, true);
            updateStatus(game);

            // 2. ¡NUEVO! Detectar si la CPU acertó
            const shots = game.playerBoard.shotsReceived;
            if (shots.length > 0) {
                // El último disparo es el que acaba de hacer la CPU
                const lastShot = shots[shots.length - 1];

                let hit = false;
                for (let ship of game.playerBoard.ships) {
                    if (ship.cells.includes(lastShot)) {
                        hit = true;
                        break;
                    }
                }

                if (hit) {
                    soundBoom.play();
                    // Mostramos explosión en PLAYER-BOARD (¡Porque nos han dado!)
                    showExplosion(lastShot, "player-board");
                } else {
                    soundWater.play();
                }
            }
        }
    } catch (error) {
        console.error("Error CPU:", error);
    }
}

// 4. PINTAR TABLEROS
function updateBoard(elementId, boardData, isEnemy) {
    const boardElement = document.getElementById(elementId);
    boardElement.innerHTML = "";

    for (let r = 0; r < 10; r++) {
        for (let c = 0; c < 10; c++) {
            const cell = document.createElement("div");
            cell.className = "cell";
            const rowChar = String.fromCharCode(65 + r);
            const coord = rowChar + (c + 1);
            cell.dataset.coord = coord;

            if (!isEnemy) {
                for (let ship of boardData.ships) {
                    if (ship.cells.includes(coord)) {
                        cell.classList.add("ship");
                        if (ship.sunk) cell.classList.add("sunk");
                        break;
                    }
                }
            } else {
                for (let ship of boardData.ships) {
                    if (ship.sunk && ship.cells.includes(coord)) {
                        cell.classList.add("sunk");
                        cell.classList.add("ship");
                    }
                }
            }

            if (boardData.shotsReceived.includes(coord)) {
                let hitShip = null;
                for (let ship of boardData.ships) {
                    if (ship.cells.includes(coord)) {
                        hitShip = ship; break;
                    }
                }

                if (hitShip) {
                    if (hitShip.sunk) {
                        cell.classList.add("skull-cell");
                        cell.classList.add("ship");
                    } else {
                        cell.classList.add("hit");
                        cell.classList.add("ship");
                    }
                } else {
                    cell.classList.add("water");
                }
            }

            if (isEnemy) {
                cell.onclick = () => fire(coord);
            }
            boardElement.appendChild(cell);
        }
    }
}

// 5. ESTADO
function updateStatus(game) {
    const statusText = document.getElementById("game-status");
    const turnText = document.getElementById("turn-indicator");

    if (game.status === "FINISHED") {
        gameFinished = true;
        statusText.innerText = "Partida finalizada";
        turnText.innerText = "";
        showGameOverModal(game.winner);
        return;
    }

    if (game.turn === "PLAYER") {
        turnText.innerText = "PLAYER TURN... 🟢";
        statusText.innerText = "WAITING FOR COORDINATES...";
    } else {
        turnText.innerText = "CPU TURN... 🔴";
        statusText.innerText = "CALCULATING COORDINATES...";
        setTimeout(() => { playCpuTurn(); }, 1500);
    }
}

function showGameOverModal(winner) {
    const modal = document.getElementById("game-over-modal");
    const title = document.getElementById("game-result-title");

    modal.style.display = "flex";

    if (winner === "PLAYER") {
        title.innerText = "YOU WIN! 🏆";
        title.className = "win-text";
        launchConfetti();
    } else {
        title.innerText = "YOU LOSE ☠️";
        title.className = "lose-text";
    }
}

function restartGame() {
    stopConfetti();
    createGame();
}

function exitToMenu() {
    stopConfetti();

    document.getElementById("game-over-modal").style.display = "none";
    document.getElementById("game-panel").style.display = "none";

    document.getElementById("login-panel").style.display = "inline-block";
    document.getElementById("full-screen-bg").style.display = "block";
    document.getElementById("game-title").style.display = "block";

    document.getElementById("username").value = "";
    currentUsername = "";
}

// CONFETI
let confettiActive = false;

function launchConfetti() {
    confettiActive = true;
    (function frame() {
        if (!confettiActive) return;
        confetti({ particleCount: 7, angle: 60, spread: 55, origin: { x: 0 }, zIndex: 9999, colors: ['#27ae60', '#f1c40f', '#e74c3c'] });
        confetti({ particleCount: 7, angle: 120, spread: 55, origin: { x: 1 }, zIndex: 9999, colors: ['#27ae60', '#f1c40f', '#e74c3c'] });
        requestAnimationFrame(frame);
    }());
}

function stopConfetti() {
    confettiActive = false;
    confetti.reset();
}

// --- FUNCIÓN PARA MOSTRAR EXPLOSIÓN (MEJORADA) ---
// Ahora acepta 'boardId' para saber en qué tablero pintar
function showExplosion(coordinate, boardId) {
    const board = document.getElementById(boardId); // "cpu-board" o "player-board"
    if (!board) return;

    const cell = board.querySelector(`.cell[data-coord="${coordinate}"]`);

    if (cell) {
        const explosionImg = document.createElement("img");
        explosionImg.src = "explosion.png";
        explosionImg.style.position = "absolute";
        explosionImg.style.top = "0";
        explosionImg.style.left = "0";
        explosionImg.style.width = "100%";
        explosionImg.style.height = "100%";
        explosionImg.style.zIndex = "10";
        explosionImg.style.pointerEvents = "none";

        cell.style.position = "relative";
        cell.appendChild(explosionImg);

        setTimeout(() => {
            if (cell.contains(explosionImg)) {
                cell.removeChild(explosionImg);
            }
        }, 500);
    }
}
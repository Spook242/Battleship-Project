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
            // 1. Ocultar Login y Portada
            document.getElementById("login-panel").style.display = "none";
            document.getElementById("full-screen-bg").style.display = "none";

            // 2. Mostrar Juego
            document.getElementById("game-panel").style.display = "block";
            document.getElementById("game-over-modal").style.display = "none";

            // 3. Pintar tableros
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

// 2. DISPARAR
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

            // SONIDOS
            let hit = false;
            for(let ship of game.cpuBoard.ships) {
                if(ship.cells.includes(coordinate)) {
                    hit = true; break;
                }
            }
            if(hit) soundBoom.play();
            else soundWater.play();

            updateBoard("player-board", game.playerBoard, false);
            updateBoard("cpu-board", game.cpuBoard, true);
            updateStatus(game);
        }
    } catch (error) {
        console.error("Error disparando:", error);
    }
}

// 3. TURNO CPU
async function playCpuTurn() {
    if (gameFinished) return;

    try {
        const response = await fetch(`${API_URL}/${currentGameId}/cpu-turn`, {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        });

        if (response.ok) {
            const game = await response.json();
            updateBoard("player-board", game.playerBoard, false);
            updateBoard("cpu-board", game.cpuBoard, true);
            updateStatus(game);
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

            // BARCOS
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

            // DISPAROS Y CALAVERAS
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
                        // cell.innerText = "💥"; // Descomenta si quieres el icono
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
        turnText.innerText = "Turno: TU TURNO 🟢";
        statusText.innerText = "Esperando tus órdenes...";
    } else {
        turnText.innerText = "Turno: CPU PENSANDO... 🔴";
        statusText.innerText = "La máquina está calculando disparo...";
        setTimeout(() => { playCpuTurn(); }, 1500);
    }
}

// 6. FUNCIONES MODAL Y MENÚ
function showGameOverModal(winner) {
    const modal = document.getElementById("game-over-modal");
    const title = document.getElementById("game-result-title");

    modal.style.display = "flex";

    if (winner === "PLAYER") {
        title.innerText = "YOU WIN! 🏆";
        title.className = "win-text";
    } else {
        title.innerText = "YOU LOSE ☠️";
        title.className = "lose-text";
    }
}

function restartGame() {
    createGame();
}

function exitToMenu() {
    document.getElementById("game-over-modal").style.display = "none";
    document.getElementById("game-panel").style.display = "none";

    // VOLVER A MOSTRAR LOGIN Y PORTADA
    document.getElementById("login-panel").style.display = "inline-block"; // O "block"
    document.getElementById("full-screen-bg").style.display = "block";

    document.getElementById("username").value = "";
    currentUsername = "";
}
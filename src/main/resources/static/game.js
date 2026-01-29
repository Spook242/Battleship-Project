const API_URL = "http://localhost:8080/game";
let currentGameId = null;
let gameFinished = false;

// 1. FUNCIÓN PARA CREAR NUEVA PARTIDA
async function createGame() {
    const username = document.getElementById("username").value;
    if (!username) {
        alert("¡Por favor, introduce tu nombre de Capitán!");
        return;
    }

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

            // Mostrar el panel de juego y ocultar login
            document.getElementById("login-panel").style.display = "none";
            document.getElementById("game-panel").style.display = "block";

            // Pintar los tableros iniciales
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

// 2. FUNCIÓN PARA DISPARAR (Turno Jugador)
async function fire(coordinate) {
    if (gameFinished) return;

    try {
        const response = await fetch(`${API_URL}/${currentGameId}/fire`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ coordinate: coordinate })
        });

        if (response.ok) {
            const game = await response.json();
            updateBoard("player-board", game.playerBoard, false);
            updateBoard("cpu-board", game.cpuBoard, true);
            updateStatus(game);
        } else {
            const error = await response.text();
            alert("Error: " + error);
        }
    } catch (error) {
        console.error("Error disparando:", error);
    }
}

// 3. FUNCIÓN PARA EL TURNO DE LA CPU (Con retraso)
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

// 4. FUNCIÓN PARA PINTAR TABLEROS
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
                    }
                }
            }

            if (boardData.shotsReceived.includes(coord)) {
                let isHit = false;
                for (let ship of boardData.ships) {
                    if (ship.cells.includes(coord)) {
                        isHit = true;
                        break;
                    }
                }

                if (isHit) {
                    cell.classList.add("hit");
                    cell.innerText = "💥";
                } else {
                    cell.classList.add("water");
                    cell.innerText = "💧";
                }
            }

            if (isEnemy) {
                cell.onclick = () => fire(coord);
            }

            boardElement.appendChild(cell);
        }
    }
}

// 5. ACTUALIZAR ESTADO (Con el temporizador de 3 seg)
function updateStatus(game) {
    const statusText = document.getElementById("game-status");
    const turnText = document.getElementById("turn-indicator");

    if (game.status === "FINISHED") {
        gameFinished = true;
        statusText.innerText = "GANADOR: " + game.winner + " 🎉";
        turnText.innerText = "Partida terminada";
        if (game.winner === "PLAYER") alert("¡HAS GANADO! 🏆");
        else alert("¡TE HAN DERROTADO! ☠️");
        return;
    }

    if (game.turn === "PLAYER") {
        turnText.innerText = "Turno: TU TURNO 🟢";
        statusText.innerText = "Esperando tus órdenes...";
    } else {
        turnText.innerText = "Turno: CPU PENSANDO... 🔴";
        statusText.innerText = "La máquina está calculando disparo...";

        setTimeout(() => {
            playCpuTurn();
        }, 3000);
    }
}
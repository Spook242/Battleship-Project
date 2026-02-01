const API_URL = "http://localhost:8080/game";
let currentGameId = null;
let gameFinished = false;
let currentUsername = "";

// SONIDOS
const soundShot = new Audio('sounds/shot.mp3');
const soundWater = new Audio('sounds/water.mp3');
const soundBoom = new Audio('sounds/boom.mp3');

// 1. CREAR PARTIDA
// 1. CREAR PARTIDA
async function createGame() {
const audio = document.getElementById("introAudio");
    if (audio) {
        // Efecto "Fade": Bajamos al 20% para jugar concentrados
        audio.volume = 0.2;
    }
    const usernameInput = document.getElementById("username");
    const errorMsg = document.getElementById("login-error"); // Referencia al mensaje

    const username = usernameInput.value || currentUsername;

    // --- CAMBIO: VALIDACIÓN VISUAL ---
    if (!username) {
        // En vez de alert, mostramos el texto en el HTML
        errorMsg.innerText = "Please enter your captain's name ⚠️";
        errorMsg.style.display = "block";
        return; // Paramos aquí
    } else {
        // Si hay nombre, ocultamos el error por si estaba visible de antes
        errorMsg.style.display = "none";
    }
    // ----------------------------------

    currentUsername = username;

    try {
        // ... (El resto de la función sigue igual que antes) ...
        const response = await fetch(`${API_URL}/new`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: username })
        });

        // ... etc ...

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

            updateBoard("player-board", game.playerBoard, false);
            updateBoard("cpu-board", game.cpuBoard, true);
            updateStatus(game); // Aquí se decide si la partida acabó

            // --- LÓGICA DEL MENSAJE Y EFECTOS ---
            let hit = false;
            let sunk = false;

            for(let ship of game.cpuBoard.ships) {
                if(ship.cells.includes(coordinate)) {
                    hit = true;
                    if(ship.sunk) sunk = true;
                    break;
                }
            }

            if(hit) {
                soundBoom.play();
                showExplosion(coordinate, "cpu-board");

                // CORRECCIÓN: Solo mostramos texto si hundimos Y la partida SIGUE VIVA.
                // Si la partida ha terminado ("FINISHED"), no ponemos texto para que no tape el modal.
                if (sunk && game.status !== "FINISHED") {
                    showShotMessage(`${coordinate} HIT AND SUNK! ☠️`, "sunk");
                }
            } else {
                soundWater.play();
            }
        }
    } catch (error) {
        console.error("Error disparando:", error);
    }
}

// 3. TURNO CPU
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

            // --- DETECTAR QUÉ HIZO LA CPU ---
            const shots = game.playerBoard.shotsReceived;
            if (shots.length > 0) {
                const lastShot = shots[shots.length - 1];

                let hit = false;
                let sunk = false;

                for (let ship of game.playerBoard.ships) {
                    if (ship.cells.includes(lastShot)) {
                        hit = true;
                        if(ship.sunk) sunk = true;
                        break;
                    }
                }

                if (hit) {
                    soundBoom.play();
                    showExplosion(lastShot, "player-board");

                    // CORRECCIÓN: Lo mismo aquí, si la CPU te mata, que no salga el texto encima del Game Over
                    if(sunk && game.status !== "FINISHED") {
                        showShotMessage(`CPU: ${lastShot} HIT AND SUNK! ☠️`, "sunk");
                    }
                } else {
                    soundWater.play();
                }
            }
        }
    } catch (error) {
        console.error("Error CPU:", error);
    }
}

// 4. PINTAR TABLEROS (CON COORDENADAS)
function updateBoard(elementId, boardData, isEnemy) {
    const boardElement = document.getElementById(elementId);
    boardElement.innerHTML = "";

    // --- A. FILA SUPERIOR (NÚMEROS 1-10) ---
    const corner = document.createElement("div");
    corner.className = "label-cell";
    boardElement.appendChild(corner);

    for (let i = 1; i <= 10; i++) {
        const label = document.createElement("div");
        label.className = "label-cell";
        label.innerText = i;
        boardElement.appendChild(label);
    }

    // --- B. FILAS DEL JUEGO (LETRA + CASILLAS) ---
    for (let r = 0; r < 10; r++) {
        const rowChar = String.fromCharCode(65 + r);
        const label = document.createElement("div");
        label.className = "label-cell";
        label.innerText = rowChar;
        boardElement.appendChild(label);

        for (let c = 0; c < 10; c++) {
            const cell = document.createElement("div");
            cell.className = "cell";
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

            // VERIFICAR SI ESA CASILLA HA RECIBIDO UN DISPARO
                        if (boardData.shotsReceived.includes(coord)) {
                            let hitShip = null;
                            // Comprobamos si el disparo tocó algún barco
                            for (let ship of boardData.ships) {
                                if (ship.cells.includes(coord)) {
                                    hitShip = ship;
                                    break;
                                }
                            }

                            if (hitShip) {
                                // SI ES TOCADO
                                if (hitShip.sunk) {
                                    cell.classList.add("skull-cell"); // Calavera si hundido
                                    cell.classList.add("ship");
                                } else {
                                    cell.classList.add("hit"); // Explosión/Rojo si tocado
                                    cell.classList.add("ship");
                                }
                            } else {
                                // 💧 AQUÍ ESTÁ EL CAMBIO: ES AGUA 💧
                                cell.classList.add("water-shot");
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
    const audio = document.getElementById("introAudio");
        if (audio) {
            audio.pause();       // Pausa la música
            audio.currentTime = 0; // Rebobina al principio (opcional)
        }
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

// --- FUNCIÓN PARA MOSTRAR EXPLOSIÓN ---
function showExplosion(coordinate, boardId) {
    const board = document.getElementById(boardId);
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

// --- NUEVA FUNCIÓN: MOSTRAR MENSAJE EN PANTALLA ---
function showShotMessage(text, type) {
    const msgDiv = document.getElementById("shot-message");

    msgDiv.innerText = text;

    msgDiv.className = "shot-message";
    msgDiv.classList.add("msg-" + type);

    msgDiv.style.display = "block";

    setTimeout(() => {
        msgDiv.style.display = "none";
    }, 1500);
}
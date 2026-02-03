const API_URL = "http://localhost:8080/game";
let currentGameId = null;
let gameFinished = false;
let currentUsername = "";

// --- SONIDOS Y EFECTOS ---
const soundShot = new Audio('sounds/shot.mp3');
const soundWater = new Audio('/water_drop.mp3');
const soundBoom = new Audio('/explosion_2.mp3');
soundBoom.volume = 0.25;

// 1. CREAR PARTIDA
async function createGame() {
    console.log("🟢 Botón Start Battle pulsado");
    stopConfetti();

    // 1. Gestionar Audio Intro
    try {
        const audio = document.getElementById("introAudio");
        if (audio) {
            audio.volume = 0.3; // Bajar volumen al empezar
        }
        stopWinMusic(); // Asegurar que no suenan músicas de fin de partida
    } catch (e) {
        console.log("Error audio:", e);
    }

    // 2. Validar Usuario
    const usernameInput = document.getElementById("username");
    const errorMsg = document.getElementById("login-error");
    const username = usernameInput.value || currentUsername;

    if (!username) {
        if(errorMsg) {
            errorMsg.innerText = "Please enter your captain's name ⚠️";
            errorMsg.style.display = "block";
        } else {
            alert("Please enter a nickname");
        }
        return;
    }

    if(errorMsg) errorMsg.style.display = "none";
    currentUsername = username;

    // 3. Petición al Servidor
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

            // UI: Cambiar de pantalla
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

    soundShot.currentTime = 0; // Rebobinar disparo también
    soundShot.play().catch(e => console.log(e));

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

            // --- LÓGICA VISUAL JUGADOR ---
            const playerAlertPanel = document.getElementById("player-alert-panel");

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
                // 🔥 ACIERTO
                soundBoom.currentTime = 0; // <--- REBOBINAR SIEMPRE
                soundBoom.play().catch(e => console.error("Error audio boom:", e));

                showExplosion(coordinate, "cpu-board");

                playerAlertPanel.innerHTML = `
                    <div class="hit-text">${coordinate}</div>
                    <img src="explosion.png" class="hit-icon" alt="BOOM">
                `;

                if (sunk && game.status !== "FINISHED") {
                    showShotMessage(`${coordinate} HIT AND SUNK! ☠️`, "sunk");
                }
            } else {
                // 💧 FALLO (AQUÍ ESTABA EL PROBLEMA)
                soundWater.currentTime = 0; // <--- ¡SOLUCIONADO!
                soundWater.play().catch(e => console.error("Error audio water:", e));

                playerAlertPanel.innerHTML = `
                    <div class="miss-text">${coordinate}</div>
                    <img src="agua_cartoon.png" class="miss-icon" alt="Water Drop">
                `;
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

            // --- LÓGICA VISUAL CPU ---
            const shots = game.playerBoard.shotsReceived;
            const cpuAlertPanel = document.getElementById("cpu-alert-panel");

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
                    // 🔥 ACIERTO CPU
                    soundBoom.currentTime = 0; // <--- REBOBINAR SIEMPRE
                    soundBoom.play().catch(e => console.error("Error audio boom:", e));

                    showExplosion(lastShot, "player-board");

                    cpuAlertPanel.innerHTML = `
                        <div class="hit-text">${lastShot}</div>
                        <img src="explosion.png" class="hit-icon" alt="BOOM">
                    `;

                    if(sunk && game.status !== "FINISHED") {
                        showShotMessage(`CPU: ${lastShot} HIT AND SUNK! ☠️`, "sunk");
                    }
                } else {
                    // 💧 FALLO CPU
                    soundWater.currentTime = 0; // <--- ¡AQUÍ ESTÁ LA SOLUCIÓN! (REBOBINAR)
                    soundWater.play().catch(e => console.error("Error audio water:", e));

                    cpuAlertPanel.innerHTML = `
                        <div class="cpu-miss-text">${lastShot}</div>
                        <img src="agua_cartoon.png" class="cpu-miss-icon" alt="Water Drop">
                    `;
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

    // Header números
    const corner = document.createElement("div");
    corner.className = "label-cell";
    boardElement.appendChild(corner);

    for (let i = 1; i <= 10; i++) {
        const label = document.createElement("div");
        label.className = "label-cell";
        label.innerText = i;
        boardElement.appendChild(label);
    }

    // Filas
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

            // Pintar barcos
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

            // Pintar disparos
            if (boardData.shotsReceived.includes(coord)) {
                let hitShip = null;
                for (let ship of boardData.ships) {
                    if (ship.cells.includes(coord)) {
                        hitShip = ship;
                        break;
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

// 5. ESTADO DEL JUEGO
function updateStatus(game) {
    const statusText = document.getElementById("game-status");
    const turnText = document.getElementById("turn-indicator");

    if (game.status === "FINISHED") {
        // PARAR MÚSICA INTRO
        const audio = document.getElementById("introAudio");
        if (audio) {
            audio.pause();
            audio.currentTime = 0;
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
        setTimeout(() => { playCpuTurn(); }, 2000);
    }
}

function showGameOverModal(winner) {
    const modal = document.getElementById("game-over-modal");
    const title = document.getElementById("game-result-title");

    modal.style.display = "flex";

    if (winner === "PLAYER") {
        // --- CASO VICTORIA ---
        title.innerText = "YOU WIN! 🏆";
        title.className = "win-text";
        launchConfetti();

        const winAudio = document.getElementById("winAudio");
        if (winAudio) {
            winAudio.volume = 0.4;
            winAudio.play().catch(e => console.log(e));
        }

    } else {
        // --- CASO DERROTA ---
        title.innerText = "YOU LOSE ☠️";
        title.className = "lose-text";

        // 🎻 SONAR MÚSICA TRISTE
        const loseAudio = document.getElementById("loseAudio");
        if (loseAudio) {
            loseAudio.loop = true; // Aseguramos el bucle
            loseAudio.volume = 0.8;
            loseAudio.play().catch(e => console.log(e));
        }
    }
}

function restartGame() {
    stopWinMusic();
    stopConfetti();
    createGame();
}

function exitToMenu() {
    stopWinMusic();
    stopConfetti();

    document.getElementById("game-over-modal").style.display = "none";
    document.getElementById("game-panel").style.display = "none";

    document.getElementById("login-panel").style.display = "inline-block";
    document.getElementById("full-screen-bg").style.display = "block";
    document.getElementById("game-title").style.display = "block";

    document.getElementById("username").value = "";
    currentUsername = "";

    // Reactivar Intro si quieres
    const audio = document.getElementById("introAudio");
    if(audio) {
        audio.volume = 0.4;
        audio.play().catch(e=>{});
    }
}

// UTILIDADES (CORREGIDA PARA PARAR AMBOS AUDIOS)
function stopWinMusic() {
    // 1. Parar Victoria
    const winAudio = document.getElementById("winAudio");
    if (winAudio) {
        winAudio.pause();
        winAudio.currentTime = 0;
    }

    // 2. Parar Derrota (¡AÑADIDO!)
    const loseAudio = document.getElementById("loseAudio");
    if (loseAudio) {
        loseAudio.pause();
        loseAudio.currentTime = 0;
    }
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

// EXPLOSION
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
        setTimeout(() => { if (cell.contains(explosionImg)) cell.removeChild(explosionImg); }, 500);
    }
}

// MENSAJES
function showShotMessage(text, type) {
    const msgDiv = document.getElementById("shot-message");
    if(!msgDiv) return;
    msgDiv.innerText = text;
    msgDiv.className = "shot-message";
    msgDiv.classList.add("msg-" + type);
    msgDiv.style.display = "block";
    setTimeout(() => { msgDiv.style.display = "none"; }, 1500);
}
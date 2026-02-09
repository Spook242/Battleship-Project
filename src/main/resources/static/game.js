const API_URL = "http://localhost:8080/game";
let currentGameId = null;
let gameFinished = false;
let currentUsername = "";

// --- SONIDOS Y EFECTOS ---
const soundShot = new Audio('sounds/shot.mp3');
const soundWater = new Audio('/water_drop.mp3');
const soundBoom = new Audio('/explosion_2.mp3');
soundBoom.volume = 0.25;

// 1. CREAR PARTIDA (Recibe Token)
async function createGame() {
    console.log("🟢 Botón Start Battle pulsado");
    stopConfetti();

    // 1. Gestionar Audio Intro
    try {
        const audio = document.getElementById("introAudio");
        if (audio) {
            audio.volume = 0.3;
        }
        stopWinMusic();
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
            const data = await response.json();

            // DESEMPAQUETAR (Juego + Token)
            const gameObj = data.game;
            const token = data.token;

            // GUARDAR TOKEN
            localStorage.setItem("jwt_token", token);

            currentGameId = gameObj.id;
            gameFinished = false;

            // UI: Cambiar de pantalla
            document.getElementById("login-panel").style.display = "none";
            document.getElementById("full-screen-bg").style.display = "none";
            document.getElementById("game-title").style.display = "none";

            document.getElementById("game-panel").style.display = "block";
            document.getElementById("game-over-modal").style.display = "none";

            // Actualizar tableros
            updateBoard("player-board", gameObj.playerBoard, false);
            updateBoard("cpu-board", gameObj.cpuBoard, true);
            updateFleetStatusPanel("player-status-panel", gameObj.playerBoard.ships);
            updateFleetStatusPanel("cpu-status-panel", gameObj.cpuBoard.ships);
            updateStatus(gameObj);

            // Sonido Start
            const startAudio = document.getElementById("startAudio");
            if(startAudio) startAudio.play().catch(e => console.log(e));

        } else {
            alert("Error al crear la partida");
        }
    } catch (error) {
        console.error("Error:", error);
        alert("No se pudo conectar con el servidor.");
    }
}

// 2. DISPARAR (Envía Token)
async function fire(coordinate) {
    if (gameFinished) return;

    soundShot.currentTime = 0;
    soundShot.play().catch(e => console.log(e));

    // RECUPERAR TOKEN
    const token = localStorage.getItem("jwt_token");

    try {
        const response = await fetch(`${API_URL}/${currentGameId}/fire`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token // <--- ¡AQUÍ VA EL TOKEN!
            },
            body: JSON.stringify({ coordinate: coordinate })
        });

        if (response.ok) {
            const game = await response.json();

            updateBoard("player-board", game.playerBoard, false);
            updateBoard("cpu-board", game.cpuBoard, true);
            updateFleetStatusPanel("player-status-panel", game.playerBoard.ships);
            updateFleetStatusPanel("cpu-status-panel", game.cpuBoard.ships);
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
                soundBoom.currentTime = 0;
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
                // 💧 FALLO
                soundWater.currentTime = 0;
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

// 3. TURNO CPU (Envía Token)
async function playCpuTurn() {
    if (gameFinished) return;

    const token = localStorage.getItem("jwt_token");

    try {
        const response = await fetch(`${API_URL}/${currentGameId}/cpu-turn`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token // <--- ¡AQUÍ TAMBIÉN!
            }
        });

        if (response.ok) {
            const game = await response.json();

            updateBoard("player-board", game.playerBoard, false);
            updateBoard("cpu-board", game.cpuBoard, true);
            updateFleetStatusPanel("player-status-panel", game.playerBoard.ships);
            updateFleetStatusPanel("cpu-status-panel", game.cpuBoard.ships);
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
                    soundBoom.currentTime = 0;
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
                    soundWater.currentTime = 0;
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
        setTimeout(() => { playCpuTurn(); }, 1250);
    }
}

// MODAL DE FIN DE PARTIDA
// MODAL DE FIN DE PARTIDA
function showGameOverModal(winner) {
    const modal = document.getElementById("game-over-modal");
    const resultVideo = document.getElementById("result-video");

    modal.style.display = "flex";
    resultVideo.muted = true; // Aseguramos que el vídeo no tenga sonido
    resultVideo.currentTime = 0;

    if (winner === "PLAYER") {
        // --- VICTORIA: VÍDEO POPEYE ---
        resultVideo.src = "/you_win.mp4";
        resultVideo.style.border = "4px solid #f1c40f"; // Borde Dorado

        launchConfetti();

        const winAudio = document.getElementById("winAudio");
        if (winAudio) {
            winAudio.volume = 0.4;
            winAudio.play().catch(e => console.log("Error audio victoria:", e));
        }

    } else {
        // --- DERROTA: VÍDEO TUMBA ---
        resultVideo.src = "/Video_You_Lose.mp4";
        resultVideo.style.border = "4px solid #8B0000"; // Borde Rojo Sangre

        const loseAudio = document.getElementById("loseAudio");
        if (loseAudio) {
            loseAudio.loop = true;
            loseAudio.volume = 0.7;
            loseAudio.play().catch(e => console.log("Error audio derrota:", e));
        }
    }

    // Reproducir el vídeo seleccionado
    resultVideo.play().catch(e => console.log("Error al reproducir vídeo:", e));
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

    const audio = document.getElementById("introAudio");
    if(audio) {
        audio.volume = 0.4;
        audio.play().catch(e=>{});
    }
}

function stopWinMusic() {
    const winAudio = document.getElementById("winAudio");
    if (winAudio) {
        winAudio.pause();
        winAudio.currentTime = 0;
    }
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

// ==========================================
// 6. RANKING SYSTEM
// ==========================================
async function showRanking() {
    const modal = document.getElementById("ranking-modal");
    const list = document.getElementById("ranking-list");

    modal.style.display = "flex";
    list.innerHTML = "<p style='text-align:center'>📡 Intercepting communications...</p>";

    try {
        // Pedimos el ranking al Backend
        const response = await fetch(`${API_URL}/ranking`);

        if (!response.ok) throw new Error("Error fetching ranking");

        const ranking = await response.json();

        // Limpiamos la lista
        list.innerHTML = "";

        if (ranking.length === 0) {
            list.innerHTML = "<p style='text-align:center'>No victories yet. Be the first! ⚓</p>";
            return;
        }

        // Generamos la lista HTML
        ranking.forEach((player, index) => {
            const medal = index === 0 ? "🥇" : index === 1 ? "🥈" : index === 2 ? "🥉" : `#${index + 1}`;

            const item = document.createElement("div");
            item.className = "ranking-item";
            item.innerHTML = `
                <span>${medal} ${player.username}</span>
                <span class="wins-count">${player.wins} 🏆</span>
            `;
            list.appendChild(item);
        });

    } catch (error) {
        console.error("Error:", error);
        list.innerHTML = "<p style='text-align:center; color:red'>Error connecting to HQ ❌</p>";
    }
}

function closeRanking() {
    document.getElementById("ranking-modal").style.display = "none";
}

// ==========================================
// 7. PINTAR ESTADO DE FLOTA (Barcos Vivos/Hundidos)
// ==========================================
function updateFleetStatusPanel(elementId, ships) {
    const container = document.getElementById(elementId);
    if (!container) return; // Protección

    container.innerHTML = ""; // Limpiar panel

    // Ordenar barcos por tamaño (Grande arriba -> Pequeño abajo)
    const sortedShips = [...ships].sort((a, b) => b.size - a.size);

    sortedShips.forEach(ship => {
        const row = document.createElement("div");
        row.className = "ship-status-row";

        for (let i = 0; i < ship.size; i++) {
            const sq = document.createElement("div");
            sq.className = "status-sq";

            if (ship.sunk) {
                sq.classList.add("sunk"); // VERDE
            } else {
                sq.classList.add("alive"); // ROJO
            }
            row.appendChild(sq);
        }
        container.appendChild(row);
    });
}
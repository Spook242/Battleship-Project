// ui.js (COMPLETO)

export const uiManager = {

// ==========================================
    // 0. ERRORES DE LOGIN
    // ==========================================
    showLoginError(message) {
        const errorMsg = document.getElementById("login-error");
        if (errorMsg) {
            errorMsg.innerText = message;
            errorMsg.style.display = "block";
        }
    },

    hideLoginError() {
        const errorMsg = document.getElementById("login-error");
        if (errorMsg) errorMsg.style.display = "none";
    },

    // ==========================================
    // 1. CAMBIOS DE PANTALLA
    // ==========================================
    showGamePanel() {
        document.getElementById("login-panel").style.display = "none";
        document.getElementById("full-screen-bg").style.display = "none";
        document.getElementById("game-title").style.display = "none";
        document.getElementById("game-panel").style.display = "block";
        document.getElementById("game-over-modal").style.display = "none";
    },

    returnToMenu() {
        document.getElementById("game-over-modal").style.display = "none";
        document.getElementById("game-panel").style.display = "none";

        document.getElementById("login-panel").style.display = "inline-block";
        document.getElementById("full-screen-bg").style.display = "block";
        document.getElementById("game-title").style.display = "block";

        document.getElementById("username").value = "";
    },

    // ==========================================
    // 2. TEXTOS DE ESTADO (Turnos)
    // ==========================================
    updateStatusText(statusMessage, turnMessage) {
        document.getElementById("game-status").innerText = statusMessage;
        document.getElementById("turn-indicator").innerText = turnMessage;
    },

    // ==========================================
    // 3. PINTAR EL TABLERO (10x10)
    // ==========================================
    updateBoard(elementId, boardData, isEnemy, onCellClick = null) {
        const boardElement = document.getElementById(elementId);
        boardElement.innerHTML = "";

        const corner = document.createElement("div");
        corner.className = "label-cell";
        boardElement.appendChild(corner);

        for (let i = 1; i <= 10; i++) {
            const label = document.createElement("div");
            label.className = "label-cell";
            label.innerText = i;
            boardElement.appendChild(label);
        }

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

                let cellHasShip = false;
                let isSunk = false;

                for (let ship of boardData.ships) {
                    if (ship.cells.includes(coord)) {
                        cellHasShip = true;
                        if (ship.sunk) isSunk = true;
                        break;
                    }
                }

                if (cellHasShip) {
                    if (!isEnemy || isSunk) {
                        cell.classList.add("ship");
                        if (isSunk) cell.classList.add("sunk");
                    }
                }

                if (boardData.shotsReceived.includes(coord)) {
                    if (cellHasShip) {
                        cell.classList.add("ship");
                        cell.classList.add(isSunk ? "skull-cell" : "hit");
                    } else {
                        cell.classList.add("water-shot");
                    }
                }

                if (isEnemy && onCellClick) {
                    cell.onclick = () => onCellClick(coord);
                }

                boardElement.appendChild(cell);
            }
        }
    },

    // ==========================================
    // 4. PINTAR EL ESTADO DE LA FLOTA
    // ==========================================
    updateFleetStatusPanel(elementId, ships) {
        const container = document.getElementById(elementId);
        if (!container) return;

        container.innerHTML = "";

        const sortedShips = [...ships].sort((a, b) => b.size - a.size);

        sortedShips.forEach(ship => {
            const row = document.createElement("div");
            row.className = "ship-status-row";

            for (let i = 0; i < ship.size; i++) {
                const sq = document.createElement("div");
                sq.className = "status-sq";
                sq.classList.add(ship.sunk ? "sunk" : "alive");
                row.appendChild(sq);
            }
            container.appendChild(row);
        });
    },

    // ==========================================
    // 5. EFECTOS VISUALES (Explosiones y Alertas)
    // ==========================================
    showExplosion(coordinate, boardId) {
        const board = document.getElementById(boardId);
        if (!board) return;
        const cell = board.querySelector(`.cell[data-coord="${coordinate}"]`);

        if (cell) {
            const explosionImg = document.createElement("img");
            explosionImg.src = "images/explosion.png";
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
                if (cell.contains(explosionImg)) cell.removeChild(explosionImg);
            }, 500);
        }
    },

    showShotMessage(text, type) {
        const msgDiv = document.getElementById("shot-message");
        if(!msgDiv) return;
        msgDiv.innerText = text;
        msgDiv.className = "shot-message msg-" + type;
        msgDiv.style.display = "block";
        setTimeout(() => { msgDiv.style.display = "none"; }, 2500);
    },

    updateAlertPanel(panelId, coordinate, isHit, isEnemyTarget) {
        const panel = document.getElementById(panelId);
        if (!panel) return;

        if (isHit) {
            panel.innerHTML = `
                <div class="hit-text">${coordinate}</div>
                <img src="images/explosion.png" class="hit-icon" alt="BOOM">
            `;
        } else {
            const textClass = isEnemyTarget ? "miss-text" : "cpu-miss-text";
            const iconClass = isEnemyTarget ? "miss-icon" : "cpu-miss-icon";

            panel.innerHTML = `
                <div class="${textClass}">${coordinate}</div>
                <img src="images/agua_cartoon.png" class="${iconClass}" alt="Water Drop">
            `;
        }
    },

    clearAlertPanels() {
        document.getElementById("player-alert-panel").innerHTML = "";
        document.getElementById("cpu-alert-panel").innerHTML = "";
    },

    // ==========================================
    // 6. MODAL DE FIN DE PARTIDA
    // ==========================================
    showGameOverModal(winner) {
        const modal = document.getElementById("game-over-modal");
        const resultVideo = document.getElementById("result-video");

        modal.style.display = "flex";
        resultVideo.muted = true;
        resultVideo.currentTime = 0;
        resultVideo.loop = true;

        if (winner === "PLAYER") {
            resultVideo.src = "videos/you_win.mp4";
            resultVideo.style.border = "4px solid #f1c40f";
            this.launchConfetti();
        } else {
            resultVideo.src = "videos/Video_You_Lose.mp4";
            resultVideo.style.border = "4px solid #8B0000";
        }

        resultVideo.play().catch(e => console.log("Error al reproducir vídeo:", e));
    },

    stopVideo() {
        const resultVideo = document.getElementById("result-video");
        if (resultVideo) {
            resultVideo.pause();
            resultVideo.currentTime = 0;
            resultVideo.src = "";
        }
    },

    // ==========================================
    // 7. SISTEMA DE RANKING
    // ==========================================
    showRankingLoading() {
        const modal = document.getElementById("ranking-modal");
        const list = document.getElementById("ranking-list");
        modal.style.display = "flex";
        list.innerHTML = "<p style='text-align:center'>📡 Intercepting communications...</p>";
    },

    showRankingError() {
        const list = document.getElementById("ranking-list");
        list.innerHTML = "<p style='text-align:center; color:red'>Error connecting to HQ ❌</p>";
    },

    renderRankingList(rankingData) {
        const list = document.getElementById("ranking-list");
        list.innerHTML = "";

        if (rankingData.length === 0) {
            list.innerHTML = "<p style='text-align:center'>No victories yet. Be the first! ⚓</p>";
            return;
        }

        rankingData.forEach((player, index) => {
            const medal = index === 0 ? "🥇" : index === 1 ? "🥈" : index === 2 ? "🥉" : `#${index + 1}`;
            const item = document.createElement("div");
            item.className = "ranking-item";
            item.innerHTML = `
                <span>${medal} ${player.username}</span>
                <span class="wins-count">${player.wins} 🏆</span>
            `;
            list.appendChild(item);
        });
    },

    closeRanking() {
        document.getElementById("ranking-modal").style.display = "none";
    },

    // ==========================================
    // 8. CONFETI
    // ==========================================
    launchConfetti() {
        window.confettiActive = true;
        (function frame() {
            if (!window.confettiActive) return;
            confetti({ particleCount: 7, angle: 60, spread: 55, origin: { x: 0 }, zIndex: 9999, colors: ['#27ae60', '#f1c40f', '#e74c3c'] });
            confetti({ particleCount: 7, angle: 120, spread: 55, origin: { x: 1 }, zIndex: 9999, colors: ['#27ae60', '#f1c40f', '#e74c3c'] });
            requestAnimationFrame(frame);
        }());
    },

    stopConfetti() {
        window.confettiActive = false;
        if(typeof confetti !== 'undefined') confetti.reset();
    }
}; // <-- ¡Esta llave era la que faltaba seguramente!
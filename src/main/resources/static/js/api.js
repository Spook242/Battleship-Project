// api.js

const API_URL = "http://localhost:8080/game";

export const api = {

    // 1. Crear una nueva partida
    async createGame(username) {
        const response = await fetch(`${API_URL}/new`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username })
        });

        if (!response.ok) throw new Error("Error al crear la partida en el servidor.");
        return await response.json();
    },

    // 2. Enviar los barcos colocados para empezar la batalla
    async startBattle(gameId, ships, token) {
        const response = await fetch(`${API_URL}/${gameId}/start-battle`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify(ships)
        });

        if (!response.ok) throw new Error("Error al iniciar la batalla.");
        return await response.json();
    },

    // 3. Realizar un disparo (Jugador)
    async fire(gameId, coordinate, token) {
        const response = await fetch(`${API_URL}/${gameId}/fire`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({ coordinate })
        });

        if (!response.ok) throw new Error("Error al registrar el disparo.");
        return await response.json();
    },

    // 4. Solicitar el turno de la CPU
    async playCpuTurn(gameId, token) {
        const response = await fetch(`${API_URL}/${gameId}/cpu-turn`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            }
        });

        if (!response.ok) throw new Error("Error al ejecutar el turno de la CPU.");
        return await response.json();
    },

    // 5. Obtener el Ranking (No requiere token)
    async getRanking() {
        const response = await fetch(`${API_URL}/ranking`);

        if (!response.ok) throw new Error("Error al obtener el ranking.");
        return await response.json();
    }
};
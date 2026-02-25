const API_URL = "http://localhost:8080/game";
const AUTH_URL = "http://localhost:8080/api/auth";

export const api = {

async register(username, password) {
        const response = await fetch(`${AUTH_URL}/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || "Register error.");
        }
        return await response.json();
    },

    async login(username, password) {
        const response = await fetch(`${AUTH_URL}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            throw new Error("Incorrect credentials");
        }
        return await response.json();
    },

    async createGame(username) {
        const response = await fetch(`${API_URL}/new`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username })
        });

        if (!response.ok) throw new Error("Error creating game.");
        return await response.json();
    },

    async startBattle(gameId, ships, token) {
        const response = await fetch(`${API_URL}/${gameId}/start-battle`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify(ships)
        });

        if (!response.ok) throw new Error("Error starting battle.");
        return await response.json();
    },

    async fire(gameId, coordinate, token) {
        const response = await fetch(`${API_URL}/${gameId}/fire`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({ coordinate })
        });

        if (!response.ok) throw new Error("Error registering the shot.");
        return await response.json();
    },

    async playCpuTurn(gameId, token) {
        const response = await fetch(`${API_URL}/${gameId}/cpu-turn`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            }
        });

        if (!response.ok) throw new Error("Error executing CPU turn.");
        return await response.json();
    },

    async getRanking() {
        const response = await fetch(`${API_URL}/ranking`);

        if (!response.ok) throw new Error("Error obtaining ranking.");
        return await response.json();
    }
};
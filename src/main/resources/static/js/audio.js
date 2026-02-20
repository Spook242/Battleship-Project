// audio.js

// 1. Cargamos todos los efectos de sonido
const effects = {
    shot: new Audio('sounds/shot.mp3'),
    water: new Audio('sounds/water_drop.mp3'),
    boom: new Audio('sounds/explosion_2.mp3'),
    mayday: new Audio('sounds/mayday.wav'),
    hammer: new Audio('sounds/hammer.mp3'),
    error: new Audio('sounds/error.wav')
};

// 2. Ajustamos los volúmenes iniciales
effects.boom.volume = 0.35;
effects.mayday.volume = 0.7;
effects.hammer.volume = 0.6;
effects.error.volume = 0.85;
effects.water.volume = 1;

// 3. Exportamos el "Manager" que usará tu juego
export const audioManager = {

    // --- EFECTOS CORTOS ---
    playShot() {
        effects.shot.currentTime = 0;
        effects.shot.play().catch(e => console.error("Error audio shot:", e));
    },
    playWater() {
        effects.water.currentTime = 0;
        effects.water.play().catch(e => console.error("Error audio water:", e));
    },
    playBoom() {
        effects.boom.currentTime = 0;
        effects.boom.play().catch(e => console.error("Error audio boom:", e));
    },
    playMayday() {
        effects.mayday.currentTime = 0;
        effects.mayday.play().catch(e => console.error("Error audio mayday:", e));
    },
    playHammer() {
        effects.hammer.currentTime = 0;
        effects.hammer.play().catch(e => console.error("Error audio hammer:", e));
    },
    playError() {
        effects.error.currentTime = 0;
        effects.error.play().catch(e => console.error("Error audio error:", e));
    },

    // --- MÚSICA DE FONDO (Desde el HTML) ---
    playIntro() {
        const audio = document.getElementById("introAudio");
        if (audio) {
            audio.volume = 0.30;
            // Solo le damos al play si estaba pausada. Si ya está sonando, la deja en paz.
            if (audio.paused) {
                audio.play().catch(e => console.error("Error intro:", e));
            }
        }
    },

    stopIntro() {
        const audio = document.getElementById("introAudio");
        if (audio) {
            audio.pause();
            audio.currentTime = 0;
        }
    },

    playWin() {
        const audio = document.getElementById("winAudio");
        if (audio) {
            audio.volume = 0.4;
            audio.currentTime = 0;
            audio.play().catch(e => console.error(e));
        }
    },

    playLose() {
        const audio = document.getElementById("loseAudio");
        if (audio) {
            audio.loop = true;
            audio.volume = 0.7;
            audio.currentTime = 0;
            audio.play().catch(e => console.error(e));
        }
    },

    stopAllMusic() {
        // Al quitar 'this.stopIntro()' de aquí, la música principal no se cortará al darle a Start Battle
        const winAudio = document.getElementById("winAudio");
        if (winAudio) { winAudio.pause(); winAudio.currentTime = 0; }

        const loseAudio = document.getElementById("loseAudio");
        if (loseAudio) { loseAudio.pause(); loseAudio.currentTime = 0; }
    }
}; // <--- ¡ESTA ERA LA LLAVE QUE SE HABÍA PERDIDO!
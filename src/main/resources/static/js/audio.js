const effects = {
    shot: new Audio('sounds/shot.mp3'),
    water: new Audio('sounds/water_drop.mp3'),
    boom: new Audio('sounds/explosion_2.mp3'),
    mayday: new Audio('sounds/mayday.wav'),
    hammer: new Audio('sounds/hammer.mp3'),
    error: new Audio('sounds/error.wav')
};

effects.boom.volume = 0.3;
effects.mayday.volume = 0.7;
effects.hammer.volume = 0.6;
effects.error.volume = 0.9;
effects.water.volume = 1;


export const audioManager = {

    playShot() {
        effects.shot.currentTime = 0;
        effects.shot.play().catch(e => console.error("Error audio shot:", e));

    },
    playSonar() {
        const sonar = document.getElementById("sonarAudio");
        if (sonar) {
            sonar.volume = 1.0;
            sonar.currentTime = 0; 
            sonar.play().catch(e => console.error("Error reproduciendo sonar:", e));
        }
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

    
    playIntro() {
        const audio = document.getElementById("introAudio");
        if (audio) {
            audio.volume = 0.3;
            
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
            audio.volume = 0.25;
            audio.currentTime = 0;
            audio.play().catch(e => console.error(e));
        }
    },

    playLose() {
        const audio = document.getElementById("loseAudio");
        if (audio) {
            audio.loop = true;
            audio.volume = 0.5;
            audio.currentTime = 0;
            audio.play().catch(e => console.error(e));
        }
    },

    stopAllMusic() {
        
        const winAudio = document.getElementById("winAudio");
        if (winAudio) { winAudio.pause(); winAudio.currentTime = 0; }

        const loseAudio = document.getElementById("loseAudio");
        if (loseAudio) { loseAudio.pause(); loseAudio.currentTime = 0; }
    }
}; 
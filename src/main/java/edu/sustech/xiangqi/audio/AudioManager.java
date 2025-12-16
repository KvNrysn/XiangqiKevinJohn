package edu.sustech.xiangqi.audio;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.Map;

public final class AudioManager {

    // ================== FLAGS ==================
    private static boolean musicEnabled = true;
    private static boolean sfxEnabled = true;

    // ================== BGM ==================
    private static Clip bgmClip;
    private static FloatControl bgmVolume;

    // Persisted volume state (0.0f – 1.0f)
    private static float musicVolume = 0.6f;
    private static float sfxVolume = 0.6f; // kept for future use

    // ================== SFX REGISTRY ==================
    private static final Map<String, String> SFX_MAP = Map.of(
            "click",     "/audio/click.wav",
            "move",      "/audio/move.wav",
            "capture",   "/audio/capture.wav",
            "check",     "/audio/check.wav",
            "illegal",   "/audio/illegal.wav",
            "gameover",  "/audio/gameover.wav"
    );

    // ================== INIT ==================
    public static void init() {
        initBGM("/audio/tenxi.wav");
        setMusicVolume(musicVolume); // apply default volume
    }

    private static void initBGM(String path) {
        try {
            URL url = AudioManager.class.getResource(path);
            if (url == null) {
                throw new RuntimeException("BGM not found: " + path);
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);

            bgmVolume = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);

            if (musicEnabled) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize BGM");
            e.printStackTrace();
        }
    }

    // ================== MUSIC CONTROL ==================
    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (bgmClip == null) return;

        if (enabled) {
            if (!bgmClip.isRunning()) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } else {
            bgmClip.stop();
        }
    }

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static void setMusicVolume(float volume) {
        // clamp
        musicVolume = Math.max(0f, Math.min(1f, volume));

        if (bgmVolume == null) return;

        float min = bgmVolume.getMinimum();
        float max = bgmVolume.getMaximum();
        bgmVolume.setValue(min + (max - min) * musicVolume);
    }

    public static float getMusicVolume() {
        return musicVolume;
    }

    // ================== SFX ==================
    public static void playSFX(String key) {
        if (!sfxEnabled) return;

        String path = SFX_MAP.get(key);
        if (path == null) {
            System.err.println("Unknown SFX key: " + key);
            return;
        }

        try {
            URL url = AudioManager.class.getResource(path);
            if (url == null) {
                System.err.println("Missing SFX file: " + path);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start();

        } catch (Exception e) {
            System.err.println("Failed to play SFX: " + key);
            e.printStackTrace();
        }
    }

    public static void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;
    }

    public static boolean isSfxEnabled() {
        return sfxEnabled;
    }

    private AudioManager() {}
}

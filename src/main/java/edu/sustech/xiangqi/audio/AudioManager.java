package edu.sustech.xiangqi.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class AudioManager {

    private static Clip bgmClip;
    private static boolean enabled = true;
    private static float volume = 0.6f;

    private AudioManager() {}

    // Call ONCE at app startup
    public static void init() {
        if (bgmClip != null) return;

        try {
            InputStream raw =
                    AudioManager.class.getResourceAsStream("/audio/tenxi.wav");

            if (raw == null) {
                throw new RuntimeException("BGM not found: /audio/tenxi.wav");
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(raw)
            );

            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            applyVolume();

            if (enabled) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }

        } catch (Exception e) {
            System.err.println("Failed to init background music:");
            e.printStackTrace();
        }
    }

    public static void play() {
        if (!enabled || bgmClip == null) return;

        if (!bgmClip.isRunning()) {
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void stop() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    public static void setEnabled(boolean on) {
        enabled = on;
        if (on) play();
        else stop();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        applyVolume();
    }

    public static float getVolume() {
        return volume;
    }

    private static void applyVolume() {
        if (bgmClip == null) return;

        if (bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain =
                    (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);

            float min = gain.getMinimum();
            float max = gain.getMaximum();
            gain.setValue(min + (max - min) * volume);
        }
    }
}

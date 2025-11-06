package de.slimenest.voicechatinteraction.util;

/**
 * Provides static methods for analyzing audio sample arrays.
 * All methods are thread-safe and stateless.
 *
 * @author SlimeNest (restructured)
 */
public final class SoundAnalyzer {
    // Prevent instantiation
    private SoundAnalyzer() {
    }

    /**
     * Computes the decibel (dB) value of a PCM sample array.
     * Returns -127 if the array is null or empty.
     *
     * @param pcmSamples array of signed 16-bit PCM samples
     * @return decibel value in range [-127, 0]
     */
    public static double computeDecibelLevel(short[] pcmSamples) {
        if (pcmSamples == null || pcmSamples.length == 0)
            return -127D;
        double sumSquares = 0.0;
        int count = 0;
        for (short s : pcmSamples) {
            double normalized = s / (double) Short.MAX_VALUE;
            sumSquares += normalized * normalized;
            count++;
        }
        if (count == 0)
            return -127D;
        double rms = Math.sqrt(sumSquares / count);
        double db = (rms > 0.0) ? 20.0 * Math.log10(rms) : -127.0;
        if (db < -127.0)
            db = -127.0;
        if (db > 0.0)
            db = 0.0;
        return db;
    }
}

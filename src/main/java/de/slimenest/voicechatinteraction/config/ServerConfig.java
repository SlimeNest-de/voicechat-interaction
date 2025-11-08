package de.slimenest.voicechatinteraction.config;


import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration data for the VoiceChatInteraction plugin.
 *
 * Reads values from config.yml and validates them.
 */
public class ServerConfig {
    // Improved config field names
    public final boolean enableGroupVoice;
    public final boolean enableWhisperVoice;
    public final boolean enableSneakVoice;
    public final int activationDbThreshold;
    public final boolean toggleDefaultState;
    public final int activationCooldownTicks;

    /**
     * Reads and validates the configuration.
     * 
     * @param config Bukkit configuration
     */
    public ServerConfig(final FileConfiguration config) {
        enableGroupVoice = config.getBoolean("enable_group_voice", false);
        enableWhisperVoice = config.getBoolean("enable_whisper_voice", false);
        enableSneakVoice = config.getBoolean("enable_sneak_voice", false);
        activationDbThreshold = validateDbThreshold(config.getInt("activation_db_threshold", -50));
        toggleDefaultState = config.getBoolean("toggle_default_state", true);
        activationCooldownTicks = validateCooldownTicks(config.getInt("activation_cooldown_ticks", 20));
    }

    /**
     * Validates and returns a proper dB threshold value.
     */
    private static int validateDbThreshold(final int value) {
        if (value < -127 || value > 0) {
            Bukkit.getLogger().warning("[voicechat_interaction] Invalid activation_db_threshold (" + value + 
                "). Must be between -127 and 0. Using default (-50).");
            return -50;
        }
        return value;
    }

    /**
     * Validates and returns a proper cooldown ticks value.
     */
    private static int validateCooldownTicks(final int value) {
        if (value < 0) {
            Bukkit.getLogger().warning("[voicechat_interaction] Invalid activation_cooldown_ticks (" + value + 
                "). Must be >= 0. Using default (20).");
            return 20;
        }
        return value;
    }
}

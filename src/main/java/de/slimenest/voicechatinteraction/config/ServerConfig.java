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
    public ServerConfig(FileConfiguration config) {
        enableGroupVoice = config.getBoolean("enable_group_voice", false);
        enableWhisperVoice = config.getBoolean("enable_whisper_voice", false);
        enableSneakVoice = config.getBoolean("enable_sneak_voice", false);
        int minDb = config.getInt("activation_db_threshold", -50);
        if (minDb < -127 || minDb > 0) {
            activationDbThreshold = -50;
            Bukkit.getLogger().warning("[voicechat_interaction] Warning: activation_db_threshold should be between -127 and 0! Using default (-50).");
        } else {
            activationDbThreshold = minDb;
        }
        toggleDefaultState = config.getBoolean("toggle_default_state", true);
        int cd = config.getInt("activation_cooldown_ticks", 20);
        activationCooldownTicks = (cd < 0) ? 20 : cd;
    }
}

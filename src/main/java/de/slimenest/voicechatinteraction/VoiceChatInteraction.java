package de.slimenest.voicechatinteraction;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.slimenest.voicechatinteraction.command.VoiceChatInteractionCommand;
import de.slimenest.voicechatinteraction.config.ServerConfig;
import de.slimenest.voicechatinteraction.util.MessageProvider;
import org.bukkit.GameEvent;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

/**
 * Main plugin class for VoiceChat Interaction.
 * Handles initialization, config, commands, and event bridge registration.
 */
public final class VoiceChatInteraction extends JavaPlugin {

    public static final String PLUGIN_ID = "voicechat_interaction";
    public static VoiceChatInteraction instance;
    public static java.util.logging.Logger logger;
    public static ServerConfig config;
    public static MessageProvider messages;
    public static Server bukkitServer;
    public static GameEvent voiceGameEvent;

    @Nullable
    public static VoiceChatEventBridge voiceChatBridge;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        bukkitServer = getServer();
        voiceGameEvent = GameEvent.EAT; // EAT triggers sculk sensors (sound event)
        loadPluginConfig();
        messages = new MessageProvider(this);
        registerVoiceChatBridge();
        registerCommands();
    }

    @Override
    public void onDisable() {
        if (voiceChatBridge != null) {
            bukkitServer.getServicesManager().unregister(voiceChatBridge);
            logger.info("Successfully unregistered voicechat_interaction event bridge");
        }
    }

    /**
     * Initializes and loads the plugin configuration.
     * Sets sensible defaults for all config values.
     */
    private void loadPluginConfig() {
        FileConfiguration fileConfig = this.getConfig();
        fileConfig.addDefault("enable_group_voice", false);
        fileConfig.addDefault("enable_whisper_voice", false);
        fileConfig.addDefault("enable_sneak_voice", false);
        fileConfig.addDefault("activation_db_threshold", -50);
        fileConfig.addDefault("toggle_default_state", true);
        fileConfig.addDefault("activation_cooldown_ticks", 20);
        fileConfig.options().copyDefaults(true);
        saveConfig();
        config = new ServerConfig(fileConfig);
    }

    /**
     * Registers the voice chat event bridge with the Simple Voice Chat API.
     */
    private void registerVoiceChatBridge() {
        BukkitVoicechatService svc = bukkitServer.getServicesManager().load(BukkitVoicechatService.class);
        if (svc != null) {
            voiceChatBridge = new VoiceChatEventBridge();
            svc.registerPlugin(voiceChatBridge);
            logger.info("Successfully registered voicechat_interaction event bridge");
        } else {
            logger.warning("Failed to register voicechat_interaction event bridge");
        }
    }

    /**
     * Registers plugin commands and tab completers.
     */
    private void registerCommands() {
        VoiceChatInteractionCommand handler = new VoiceChatInteractionCommand();
        this.getCommand("voicechat_interaction").setExecutor(handler);
        this.getCommand("voicechat_interaction").setTabCompleter(handler);
    }
}

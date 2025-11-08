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
        try {
            instance = this;
            logger = getLogger();
            bukkitServer = getServer();
            voiceGameEvent = GameEvent.EAT; // EAT triggers sculk sensors (sound event)
            
            loadPluginConfig();
            messages = new MessageProvider(this);
            registerVoiceChatBridge();
            registerCommands();
            
            logger.info("VoiceChat Interaction plugin successfully enabled!");
        } catch (final Exception e) {
            logger.severe("Failed to enable VoiceChat Interaction plugin: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
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
        try {
            final FileConfiguration fileConfig = this.getConfig();
            setConfigDefaults(fileConfig);
            fileConfig.options().copyDefaults(true);
            saveConfig();
            config = new ServerConfig(fileConfig);
            logger.info("Configuration loaded successfully");
        } catch (final Exception e) {
            logger.severe("Failed to load configuration: " + e.getMessage());
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    /**
     * Sets default configuration values.
     */
    private void setConfigDefaults(final FileConfiguration fileConfig) {
        fileConfig.addDefault("enable_group_voice", false);
        fileConfig.addDefault("enable_whisper_voice", false);
        fileConfig.addDefault("enable_sneak_voice", false);
        fileConfig.addDefault("activation_db_threshold", -50);
        fileConfig.addDefault("toggle_default_state", true);
        fileConfig.addDefault("activation_cooldown_ticks", 20);
    }

    /**
     * Registers the voice chat event bridge with the Simple Voice Chat API.
     */
    private void registerVoiceChatBridge() {
        final BukkitVoicechatService service = bukkitServer.getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            voiceChatBridge = new VoiceChatEventBridge();
            service.registerPlugin(voiceChatBridge);
            logger.info("Successfully registered voicechat_interaction event bridge");
        } else {
            logger.warning("Simple Voice Chat service not available - event bridge registration failed");
            logger.warning("Make sure Simple Voice Chat plugin is installed and enabled");
        }
    }

    /**
     * Registers plugin commands and tab completers.
     */
    private void registerCommands() {
        final VoiceChatInteractionCommand handler = new VoiceChatInteractionCommand();
        final org.bukkit.command.PluginCommand command = this.getCommand("voicechat_interaction");
        if (command != null) {
            command.setExecutor(handler);
            command.setTabCompleter(handler);
            logger.info("Commands registered successfully");
        } else {
            logger.severe("Failed to register commands - command not found in plugin.yml");
        }
    }
}

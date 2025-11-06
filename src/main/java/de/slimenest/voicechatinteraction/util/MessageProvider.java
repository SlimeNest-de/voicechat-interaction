package de.slimenest.voicechatinteraction.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and provides translated messages from messages.yml.
 * Robust: supports loading from plugin folder and classpath, with fallback.
 */
public class MessageProvider {
    private final Map<String, String> messages;

    public MessageProvider(Plugin plugin) {
        Map<String, String> loaded = new HashMap<>();
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }
        File file = new File(pluginFolder, "messages.yml");
        YamlConfiguration config = null;
        if (!file.exists()) {
            // Copy messages.yml from resources if available
            try (InputStream in = plugin.getResource("messages.yml")) {
                if (in != null) {
                    java.nio.file.Files.copy(in, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to copy messages.yml from resources: " + e.getMessage());
            }
        }
        if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
        } else {
            // Fallback: only load from resources (dev/build-in)
            config = loadFromResource(plugin);
        }
        if (config != null) {
            for (String key : config.getKeys(false)) {
                loaded.put(key, config.getString(key, key));
            }
        }
        this.messages = Collections.unmodifiableMap(loaded);
    }

    private YamlConfiguration loadFromResource(Plugin plugin) {
        try (InputStream in = plugin.getResource("messages.yml")) {
            if (in != null) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load messages.yml from resources: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets a message by key, with optional replacements (e.g. {player}).
     * If not found, returns the key itself.
     */
    public String get(String key, Map<String, String> replacements) {
        String msg = messages.getOrDefault(key, key);
        if (replacements != null && !replacements.isEmpty()) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return msg;
    }

    public String get(String key) {
        return get(key, null);
    }
}

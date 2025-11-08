package de.slimenest.voicechatinteraction;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.slimenest.voicechatinteraction.util.SoundAnalyzer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles all voice chat event logic and player state for Sculk triggering.
 * This bridge connects the Simple Voice Chat API with Bukkit's game events.
 */
public class VoiceChatEventBridge implements VoicechatPlugin {

    private static final ConcurrentHashMap<UUID, Long> playerCooldowns = new ConcurrentHashMap<>();
    private static final NamespacedKey TOGGLE_KEY = new NamespacedKey(VoiceChatInteraction.instance, "interaction_toggle");


    @Override
    public String getPluginId() {
        return VoiceChatInteraction.PLUGIN_ID;
    }

    @Override
    public void initialize(final VoicechatApi api) {
        // API reference not needed - we use event.getVoicechat() directly
    }

    @Override
    public void registerEvents(final EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStart);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    /**
     * Handles voice chat server initialization.
     */
    private void onServerStart(final VoicechatServerStartedEvent event) {
        // Server API reference not needed - we use event.getVoicechat() directly
        VoiceChatInteraction.logger.info("Voice chat server integration established");
    }

    /**
     * Main handler for incoming microphone packets from players.
     * Processes audio and triggers Sculk events if conditions are met.
     */
    private void onMicrophonePacket(final MicrophonePacketEvent event) {
        final VoicechatConnection connection = event.getSenderConnection();
        
        // Early validation checks
        if (!isValidPacket(connection, event)) {
            return;
        }
        
        final ServerPlayer voicePlayer = connection.getPlayer();
        final Player bukkitPlayer = extractBukkitPlayer(voicePlayer);
        
        if (bukkitPlayer == null || !shouldProcessPlayer(bukkitPlayer)) {
            return;
        }
        
        final short[] audioSamples = decodeAudioSafely(event);
        if (audioSamples == null || !meetsVolumeThreshold(audioSamples)) {
            return;
        }
        
        scheduleSculkTrigger(voicePlayer, bukkitPlayer);
    }

    /**
     * Validates the incoming voice packet and connection.
     */
    private boolean isValidPacket(@Nullable final VoicechatConnection connection, final MicrophonePacketEvent event) {
        if (connection == null) {
            return false;
        }
        
        final byte[] encodedData = event.getPacket().getOpusEncodedData();
        if (encodedData.length == 0) {
            return false;
        }
        
        // Check configuration-based filters
        if (!VoiceChatInteraction.config.enableGroupVoice && connection.isInGroup()) {
            return false;
        }
        
        if (!VoiceChatInteraction.config.enableWhisperVoice && event.getPacket().isWhispering()) {
            return false;
        }
        
        return true;
    }

    /**
     * Extracts the Bukkit player from the voice chat player.
     */
    @Nullable
    private Player extractBukkitPlayer(final ServerPlayer voicePlayer) {
        if (!(voicePlayer.getPlayer() instanceof Player bukkitPlayer)) {
            VoiceChatInteraction.logger.warning("Received microphone packet from non-Bukkit player");
            return null;
        }
        return bukkitPlayer;
    }

    /**
     * Determines if the player should be processed based on interaction settings.
     */
    private boolean shouldProcessPlayer(final Player player) {
        // Check sneak interaction setting
        if (player.isSneaking() && !VoiceChatInteraction.config.enableSneakVoice) {
            return false;
        }
        
        // Check if player has interactions enabled
        return isPlayerToggleEnabled(player);
    }

    /**
     * Safely decodes audio from the voice packet.
     */
    @Nullable
    private short[] decodeAudioSafely(final MicrophonePacketEvent event) {
        final OpusDecoder decoder = event.getVoicechat().createDecoder();
        if (decoder == null) {
            VoiceChatInteraction.logger.warning("Failed to create OpusDecoder for audio processing");
            return null;
        }
        
        try {
            decoder.resetState();
            return decoder.decode(event.getPacket().getOpusEncodedData());
        } catch (final Exception e) {
            VoiceChatInteraction.logger.warning("Audio decoding failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the audio meets the configured volume threshold.
     */
    private boolean meetsVolumeThreshold(final short[] samples) {
        final double decibelLevel = SoundAnalyzer.computeDecibelLevel(samples);
        return decibelLevel >= VoiceChatInteraction.config.activationDbThreshold;
    }

    /**
     * Schedules the Sculk event trigger on the main server thread.
     */
    private void scheduleSculkTrigger(final ServerPlayer voicePlayer, final Player bukkitPlayer) {
        Bukkit.getScheduler().runTask(VoiceChatInteraction.instance, () -> {
            if (canPlayerTriggerEvent(voicePlayer.getUuid(), bukkitPlayer)) {
                bukkitPlayer.getWorld().sendGameEvent(
                    bukkitPlayer, 
                    VoiceChatInteraction.voiceGameEvent,
                    bukkitPlayer.getLocation().toVector()
                );
            }
        });
    }

    /**
     * Checks if the player can trigger a Sculk event based on cooldown and toggle state.
     */
    private boolean canPlayerTriggerEvent(final UUID playerUuid, final Player bukkitPlayer) {
        final long currentGameTime = bukkitPlayer.getWorld().getGameTime();
        final Long lastTriggerTime = playerCooldowns.get(playerUuid);
        final int cooldownTicks = VoiceChatInteraction.config.activationCooldownTicks;
        
        final boolean cooldownExpired = lastTriggerTime == null || 
                                      (currentGameTime - lastTriggerTime) > cooldownTicks;
        
        if (cooldownExpired && isPlayerToggleEnabled(bukkitPlayer)) {
            playerCooldowns.put(playerUuid, currentGameTime);
            return true;
        }
        
        return false;
    }

    /**
     * Checks if the player has voice chat interactions enabled.
     */
    public boolean isPlayerToggleEnabled(final Player player) {
        final PersistentDataContainer dataContainer = player.getPersistentDataContainer();
        
        if (!dataContainer.has(TOGGLE_KEY)) {
            return VoiceChatInteraction.config.toggleDefaultState;
        }
        
        final Byte toggleValue = dataContainer.get(TOGGLE_KEY, PersistentDataType.BYTE);
        return toggleValue != null && toggleValue != 0;
    }

    /**
     * Sets the voice chat interaction toggle state for a player.
     */
    public void setPlayerToggle(final Player player, final boolean enabled) {
        final PersistentDataContainer dataContainer = player.getPersistentDataContainer();
        dataContainer.set(TOGGLE_KEY, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }

    /**
     * Clears expired cooldown entries to prevent memory leaks.
     * Should be called periodically by the main plugin.
     */
    public static void cleanupExpiredCooldowns() {
        final long currentTime = System.currentTimeMillis();
        final long maxAge = VoiceChatInteraction.config.activationCooldownTicks * 50L; // Convert to milliseconds
        
        playerCooldowns.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > maxAge);
    }
}

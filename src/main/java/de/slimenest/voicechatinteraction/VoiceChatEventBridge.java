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
 */

public class VoiceChatEventBridge implements VoicechatPlugin {

    private static VoicechatApi voicechatApi;
    private static ConcurrentHashMap<UUID, Long> cooldownMap;

    @Nullable
    private static VoicechatServerApi voicechatServerApi;


    @Override

    public String getPluginId() {
        return VoiceChatInteraction.PLUGIN_ID;
    }

    @Override

    public void initialize(VoicechatApi api) {
        voicechatApi = api;
        cooldownMap = new ConcurrentHashMap<>();
    }

    @Override

    public void registerEvents(EventRegistration reg) {
        reg.registerEvent(VoicechatServerStartedEvent.class, this::handleServerStart);
        reg.registerEvent(MicrophonePacketEvent.class, this::handleMicPacket);
    }

    private void handleServerStart(VoicechatServerStartedEvent event) {
        voicechatServerApi = event.getVoicechat();
    }

    private void handleMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (!isValidConnection(connection, event))
            return;
        if (connection == null)
            return;
        ServerPlayer voicePlayer = connection.getPlayer();
        Player bukkitPlayer = getBukkitPlayer(voicePlayer);
        if (bukkitPlayer == null)
            return;
        if (!isInteractionAllowed(bukkitPlayer))
            return;
        short[] audio = decodeAudio(event);
        if (audio == null)
            return;
        if (!isLoudEnough(audio))
            return;
        triggerSculkIfReady(voicePlayer, bukkitPlayer);
    }

    private boolean isValidConnection(VoicechatConnection connection, MicrophonePacketEvent event) {
        if (connection == null)
            return false;
        if (event.getPacket().getOpusEncodedData().length == 0)
            return false;
        if (!VoiceChatInteraction.config.enableGroupVoice && connection.isInGroup())
            return false;
        if (!VoiceChatInteraction.config.enableWhisperVoice && event.getPacket().isWhispering())
            return false;
        return true;
    }

    private Player getBukkitPlayer(ServerPlayer voicePlayer) {
        if (!(voicePlayer.getPlayer() instanceof Player bukkitPlayer)) {
            VoiceChatInteraction.logger.warning("Received microphone packet from non-player");
            return null;
        }
        return bukkitPlayer;
    }

    private boolean isInteractionAllowed(Player player) {
        return VoiceChatInteraction.config.enableSneakVoice || !player.isSneaking();
    }

    private short[] decodeAudio(MicrophonePacketEvent event) {
        OpusDecoder decoder = event.getVoicechat().createDecoder();
        if (decoder == null) {
            VoiceChatInteraction.logger.warning("Failed to create OpusDecoder for audio decoding.");
            return new short[0];
        }
        decoder.resetState();
        return decoder.decode(event.getPacket().getOpusEncodedData());
    }

    private boolean isLoudEnough(short[] samples) {
        return SoundAnalyzer.computeDecibelLevel(samples) >= VoiceChatInteraction.config.activationDbThreshold;
    }

    private void triggerSculkIfReady(ServerPlayer voicePlayer, Player bukkitPlayer) {
        bukkitPlayer.getServer().getScheduler().runTask(VoiceChatInteraction.instance, () -> {
            if (canTrigger(voicePlayer)) {
                bukkitPlayer.getWorld().sendGameEvent(bukkitPlayer, VoiceChatInteraction.voiceGameEvent,
                        bukkitPlayer.getLocation().toVector());
            }
        });
    }

    private boolean canTrigger(ServerPlayer voicePlayer) {
        Player bukkit = Bukkit.getPlayer(voicePlayer.getUuid());
        if (bukkit == null) {
            VoiceChatInteraction.logger.warning("Tried to trigger Sculk for unknown player UUID: " + voicePlayer.getUuid());
            return false;
        }
        Long last = cooldownMap.get(voicePlayer.getUuid());
        long now = bukkit.getWorld().getGameTime();
        int cooldown = VoiceChatInteraction.config.activationCooldownTicks;
        if ((last == null || now - last > cooldown) && isToggleEnabled(bukkit)) {
            cooldownMap.put(voicePlayer.getUuid(), now);
            return true;
        }
        return false;
    }

    public boolean isToggleEnabled(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(VoiceChatInteraction.instance, "interaction_toggle");
        if (!data.has(key)) {
            return VoiceChatInteraction.config.toggleDefaultState;
        }
        return data.get(key, PersistentDataType.BYTE) != 0;
    }

    public void setToggle(Player player, boolean value) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(VoiceChatInteraction.instance, "interaction_toggle");
        data.set(key, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }
}

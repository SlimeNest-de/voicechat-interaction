package de.slimenest.voicechatinteraction.command;

import de.slimenest.voicechatinteraction.VoiceChatInteraction;
import de.slimenest.voicechatinteraction.VoiceChatEventBridge;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Handles the /voicechat_interaction command and tab completion.
 * Supports toggling voice chat interaction for self and others.
 */
public class VoiceChatInteractionCommand implements CommandExecutor, TabCompleter {

    /**
     * Handles command execution for /voicechat_interaction.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(VoiceChatInteraction.messages.get("only_players"));
            return true;
        }
        
        final VoiceChatEventBridge bridge = VoiceChatInteraction.voiceChatBridge;
        if (bridge == null) {
            player.sendMessage(VoiceChatInteraction.messages.get("bridge_not_loaded"));
            return true;
        }
        
        if (args.length == 0) {
            sendUsageMessage(player);
            return true;
        }
        
        final String subCommand = args[0].toLowerCase(Locale.ROOT);
        switch (subCommand) {
            case "toggle" -> handleToggleCommand(player, args, bridge);
            case "reload" -> handleReloadCommand(player);
            default -> player.sendMessage(VoiceChatInteraction.messages.get("unknown_command"));
        }
        return true;
    }

    /**
     * Handles the reload subcommand: reloads config and messages.
     */
    private void handleReloadCommand(final Player player) {
        if (!player.hasPermission("voicechat_interaction.command.reload")) {
            player.sendMessage(VoiceChatInteraction.messages.get("no_permission_reload"));
            return;
        }
        
        try {
            VoiceChatInteraction.instance.reloadConfig();
            VoiceChatInteraction.config = new de.slimenest.voicechatinteraction.config.ServerConfig(
                    VoiceChatInteraction.instance.getConfig());
            VoiceChatInteraction.messages = new de.slimenest.voicechatinteraction.util.MessageProvider(
                    VoiceChatInteraction.instance);
            player.sendMessage(VoiceChatInteraction.messages.get("reload_success"));
        } catch (final Exception e) {
            VoiceChatInteraction.logger.severe("Failed to reload configuration: " + e.getMessage());
            player.sendMessage(VoiceChatInteraction.messages.get("reload_failed"));
        }
    }

    /**
     * Handles the toggle subcommand for self or another player.
     */
    private void handleToggleCommand(final Player sender, final String[] args, final VoiceChatEventBridge bridge) {
        if (args.length == 1) {
            handleSelfToggle(sender, bridge);
        } else if (args.length == 2) {
            handleOtherPlayerToggle(sender, args[1], bridge);
        } else {
            sendUsageMessage(sender);
        }
    }

    /**
     * Handles toggle command for the sender themselves.
     */
    private void handleSelfToggle(final Player sender, final VoiceChatEventBridge bridge) {
        if (!sender.hasPermission("voicechat_interaction.command")) {
            sender.sendMessage(VoiceChatInteraction.messages.get("no_permission_self"));
            return;
        }
        
        final boolean newState = !bridge.isPlayerToggleEnabled(sender);
        bridge.setPlayerToggle(sender, newState);
        sendToggleMessage(sender, sender, newState, "toggle_self");
    }

    /**
     * Handles toggle command for another player.
     */
    private void handleOtherPlayerToggle(final Player sender, final String targetName, final VoiceChatEventBridge bridge) {
        if (!sender.hasPermission("voicechat_interaction.command.others")) {
            sender.sendMessage(VoiceChatInteraction.messages.get("no_permission_others"));
            return;
        }
        
        final Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(VoiceChatInteraction.messages.get("player_not_found", Map.of("player", targetName)));
            return;
        }
        
        final boolean newState = !bridge.isPlayerToggleEnabled(target);
        bridge.setPlayerToggle(target, newState);
        sendToggleMessage(sender, target, newState, "toggle_other");
    }

    /**
     * Sends the appropriate toggle message to the target player.
     */
    private void sendToggleMessage(final Player sender, final Player target, final boolean newState, final String messageKey) {
        final String stateMsg = VoiceChatInteraction.messages.get(newState ? "enabled" : "disabled");
        final Map<String, String> replacements = Map.of(
            "state", stateMsg,
            "sender", sender.getName()
        );
        target.sendMessage(VoiceChatInteraction.messages.get(messageKey, replacements));
    }

    /**
     * Sends usage information to the player.
     */
    private void sendUsageMessage(Player player) {
        player.sendMessage(VoiceChatInteraction.messages.get("usage")
                + "\n§e/voicechat_interaction reload reloads config/messages (permission: voicechat_interaction.command.reload)");
    }

    /**
     * Provides tab completion for the /voicechat_interaction command.
     */
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            final boolean canReload = sender.hasPermission("voicechat_interaction.command.reload");
            return canReload ? List.of("toggle", "reload") : List.of("toggle");
        } else if (args.length == 2 && "toggle".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("voicechat_interaction.command.others")) {
                final String input = args[1].toLowerCase(Locale.ROOT);
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                        .sorted()
                        .toList();
            }
        }
        return List.of();
    }
}

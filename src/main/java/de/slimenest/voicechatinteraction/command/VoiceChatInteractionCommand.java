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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(VoiceChatInteraction.messages.get("only_players"));
            return true;
        }
        VoiceChatEventBridge bridge = VoiceChatInteraction.voiceChatBridge;
        if (bridge == null) {
            player.sendMessage(VoiceChatInteraction.messages.get("bridge_not_loaded"));
            return true;
        }
        if (args.length == 0) {
            sendUsageMessage(player);
            return true;
        }
        String subCommand = args[0].toLowerCase(Locale.ROOT);
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
    private void handleReloadCommand(Player player) {
        if (!player.hasPermission("voicechat_interaction.command.reload")) {
            player.sendMessage(
                    "§cYou do not have permission to reload the plugin (voicechat_interaction.command.reload)");
            return;
        }
        VoiceChatInteraction.instance.reloadConfig();
        VoiceChatInteraction.config = new de.slimenest.voicechatinteraction.config.ServerConfig(
                VoiceChatInteraction.instance.getConfig());
        VoiceChatInteraction.messages = new de.slimenest.voicechatinteraction.util.MessageProvider(
                VoiceChatInteraction.instance);
        player.sendMessage("§aVoiceChat Interaction config and messages reloaded.");
    }

    /**
     * Handles the toggle subcommand for self or another player.
     */
    private void handleToggleCommand(Player sender, String[] args, VoiceChatEventBridge eventBridge) {
        VoiceChatEventBridge bridge = eventBridge;
        if (args.length == 1) {
            // Toggle for self
            if (!sender.hasPermission("voicechat_interaction.command")) {
                sender.sendMessage(VoiceChatInteraction.messages.get("no_permission_self"));
                return;
            }
            boolean toggledState = !bridge.isToggleEnabled(sender);
            bridge.setToggle(sender, toggledState);
            String stateMsg = VoiceChatInteraction.messages.get(toggledState ? "enabled" : "disabled");
            sender.sendMessage(VoiceChatInteraction.messages.get("toggle_self", Map.of("state", stateMsg)));
        } else if (args.length == 2) {
            // Toggle for another player
            if (!sender.hasPermission("voicechat_interaction.command.others")) {
                sender.sendMessage(VoiceChatInteraction.messages.get("no_permission_others"));
                return;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(VoiceChatInteraction.messages.get("player_not_found", Map.of("player", args[1])));
                return;
            }
            boolean toggledState = !bridge.isToggleEnabled(target);
            bridge.setToggle(target, toggledState);
            String stateMsg = VoiceChatInteraction.messages.get(toggledState ? "enabled" : "disabled");
            target.sendMessage(VoiceChatInteraction.messages.get("toggle_other",
                    Map.of("sender", sender.getName(), "state", stateMsg)));
        } else {
            sendUsageMessage(sender);
        }
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
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            boolean canReload = sender.hasPermission("voicechat_interaction.command.reload");
            if (canReload) {
                return List.of("toggle", "reload");
            } else {
                return List.of("toggle");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            if (sender.hasPermission("voicechat_interaction.command.others")) {
                String input = args[1].toLowerCase(Locale.ROOT);
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

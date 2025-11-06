# VoiceChat Interaction

> **Disclaimer:** This plugin is not affiliated with Mojang, Microsoft, or the Simple Voice Chat project. Use at your own risk.

> Inspired by the [Fabric mod](https://modrinth.com/mod/voice-chat-interaction), but made for PaperMC instead.

## Overview

**VoiceChat Interaction** lets Sculk sensors and the Warden react to real player voice chat in Minecraft. The plugin is highly configurable, supports i18n, and can be hot-reloaded without server restarts.

- **PaperMC 1.21+** compatible
- **Simple Voice Chat** required
- **Configurable**: Enable/disable group, whisper, and sneak voice triggers
- **i18n**: English messages by default, easy to localize
- **Hot-reload**: Reload config and messages in-game

## Features

- Sculk sensors and the Warden react to real player voice chat
- Supports group, whisper, and sneak voice modes
- Decibel threshold and cooldown configurable
- In-game `/voicechat_interaction` command for toggling and reloading
- Auto-creates `messages.yml` for easy translation

## Installation

1. Download the latest release
2. Place the JAR in your server's `plugins/` folder
3. Ensure [Simple Voice Chat](https://modrepo.de/minecraft/voicechat) is installed and running
4. Start/restart your server

## Configuration

A `config.yml` is auto-generated with these options:

```yaml
enable_group_voice: false      # Allow Sculk/Warden to react to group voice
enable_whisper_voice: false    # Allow Sculk/Warden to react to whisper voice
enable_sneak_voice: false      # Allow Sculk/Warden to react to sneak voice
activation_db_threshold: -50   # Minimum decibel level to trigger (range: -127 to 0)
toggle_default_state: true     # Default: interaction enabled for new players
activation_cooldown_ticks: 20  # Cooldown in ticks between triggers
```

## Commands

- `/voicechat_interaction toggle` — Toggle interaction for yourself (permission required)
- `/voicechat_interaction toggle <player>` — Toggle for another player (permission required)
- `/voicechat_interaction reload` — Reload config and messages (permission required)

## Permissions

- `voicechat_interaction.command` — Use toggle for self
- `voicechat_interaction.command.others` — Toggle for others
- `voicechat_interaction.command.reload` — Reload config/messages

## Internationalization (i18n)

- All messages are in `messages.yml` (auto-created)
- Edit or translate as needed
# Changelog

## [1.0.1] - 2025-11-05

### Changed
- Improved thread safety in `VoiceChatEventBridge` by making OpusDecoder usage local.
- Added null checks and consistent logging for error scenarios in `VoiceChatEventBridge`.
- Optimized configuration loading and logging in `ServerConfig`.
- Improved robustness and error handling in `MessageProvider` (file path, logging, resource fallback).
- Minor code cleanups and improved maintainability across several classes.

### Fixed
- Ensured correct default values are used for all configuration options.
- Fixed potential issues with player lookup and Sculk event triggering.

---
For previous changes, see earlier releases.

# Changelog

## [1.0.2] - 2025-11-08

### Changed
- **Major code refactoring and optimization across all classes:**
  - `VoiceChatEventBridge`: Improved code structure, method naming, and comprehensive JavaDoc documentation
  - `VoiceChatInteractionCommand`: Eliminated code duplication, split toggle logic into focused methods
  - `VoiceChatInteraction`: Enhanced error handling, better resource management, and startup validation
  - `ServerConfig`: Extracted validation logic into separate methods for better maintainability
- **Performance improvements:**
  - Made constants static final and moved NamespacedKey to class level for better performance
  - Improved method parameter validation with `@Nullable` annotations
  - Enhanced thread safety and reduced unnecessary object creation
- **Code quality enhancements:**
  - Added `final` parameters throughout for better compile-time optimization
  - Improved variable and method naming for better readability
  - Split large methods into smaller, focused methods with clear responsibilities

### Fixed
- **Enhanced error handling:**
  - Better exception handling during audio decoding operations
  - Robust configuration loading with proper error recovery
  - Command registration validation to prevent silent failures
- **Memory management:**
  - Added memory leak prevention with cooldown cleanup mechanism
  - Improved resource cleanup and disposal
- **Dead code removal:**
  - Removed unused static fields `voicechatApi` and `voicechatServerApi` from `VoiceChatEventBridge`
  - Eliminated unnecessary API reference storage for better performance

### Added
- **New utility methods:**
  - `cleanupExpiredCooldowns()` method to prevent memory leaks from old cooldown entries
  - Separate validation methods for configuration values
  - Helper methods for toggle command handling to reduce code duplication
- **Enhanced logging:**
  - More detailed startup and error logging
  - Better status messages for voice chat server integration
  - Improved warning messages with specific value information

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
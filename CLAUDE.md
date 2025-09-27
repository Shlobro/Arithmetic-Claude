# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a competitive league-based Android arithmetic practice app built with Kotlin and Jetpack Compose. The app features a sophisticated placement system and global competition framework where users compete for high scores across 23 different skill leagues, from beginner-friendly to impossibly difficult levels designed to challenge even mathematical geniuses.

## Common Development Commands

### Building and Running
- `./gradlew build` - Build the entire project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug APK to connected device/emulator

### Testing
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests (requires device/emulator)
- `./gradlew testDebugUnitTest` - Run debug unit tests specifically

### Code Quality
- `./gradlew lint` - Run Android lint checks
- `./gradlew lintDebug` - Run lint on debug variant

## Architecture

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material3
- **Target SDK**: 34, Min SDK: 26
- **Build System**: Gradle with Kotlin DSL

### Project Structure
- **Package**: `com.example.arithmiticpracticeclaude`
- **Main Activity**: `MainActivity.kt` - Entry point with navigation and placement flow
- **Core Screens**:
  - `PlacementTestScreen.kt` - One-time skill assessment for new users
  - `SimpleGameScreen.kt` / `CompactGameScreen.kt` - Main game interface
  - `LeaderboardScreen.kt` - Global competition rankings
  - `MainMenuScreen.kt` - League-themed home screen
- **Data Layer**: `data/` - Game state, leagues, placement test logic
- **Theme**: `ui/theme/` - Material3 theming with league-specific colors

### Key Components
- **League System**: 23 leagues from Rookie to Eternal with progressive difficulty
- **Placement Test**: Adaptive algorithm determining initial league placement
- **Competitive Scoring**: League-based point system with speed bonuses
- **Question Generation**: Dynamic difficulty based on current league
- **Global Leaderboard**: Score comparison and ranking system

### Game Features
- **One-Time Placement**: Permanent skill-based league assignment
- **Progressive Difficulty**:
  - Rookie: Simple addition (1-20), 25 seconds
  - Eternal: Complex operations (1-10M), 5 seconds
- **Competitive Elements**:
  - Global score comparison
  - League progression tracking
  - Time-based scoring bonuses
- **Adaptive Gameplay**: Timer and difficulty scale with league
- **Achievement System**: League-based milestones and progression

## Development Notes

The app uses a multi-screen navigation architecture with Jetpack Navigation Compose. Key architectural decisions:

- **Conditional Start Destination**: New users see placement test, returning users go to main menu
- **State Management**: Uses ViewModels with StateFlow for reactive UI updates
- **Data Persistence**: Android DataStore for game state and league progression
- **Competitive Design**: UI emphasizes league status and global competition
- **Responsive Difficulty**: Question generation and timing adapt to user's league

### League System Design

The competitive league system is the core feature:

1. **Placement Flow**: One-time adaptive test determines initial league
2. **Score Progression**: Total lifetime score determines current league
3. **Difficulty Scaling**: Each league has specific operations, number ranges, and time limits
4. **Competitive Scoring**: Higher leagues provide exponentially more points
5. **Global Competition**: Leaderboard system for score comparison

### Navigation Flow

- **First Launch**: `placement` → `menu` → `game` → `summary`
- **Returning Users**: `menu` → `game` → `summary`
- **Additional Screens**: `statistics`, `achievements`, `leaderboard`, `settings`

The app balances accessibility for children (ages 8-10 in Rookie league) with extreme challenges for mathematical experts (Eternal league with 5-second timeouts and million-digit calculations).
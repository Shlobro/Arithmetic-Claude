# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android arithmetic practice app built with Kotlin and Jetpack Compose. The app provides a simple game interface for practicing basic math operations (addition, subtraction, multiplication, and fill-in-the-blank questions).

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
- **Main Activity**: `MainActivity.kt` - Entry point that sets up Compose UI
- **Game Screen**: `ui/screens/SimpleGameScreen.kt` - Contains the main game logic and UI
- **Theme**: `ui/theme/` - Contains Color, Type, and Theme definitions

### Key Components
- `SimpleGameScreen` - Main composable containing game state and UI
- `ArithmeticQuestion` - Data class representing a math question
- Question generators for different operation types (addition, subtraction, multiplication, fill-in-blank)

### Game Features
- Score tracking
- Multiple question types (addition, subtraction, multiplication, fill-in-blank)
- Immediate feedback with correct/incorrect indication
- Question counter

## Development Notes

The app uses a single-screen architecture with all game logic contained in `SimpleGameScreen.kt`. State management is handled using Compose's `remember` and `mutableStateOf`. The UI follows Material3 design principles with proper theming structure in place.
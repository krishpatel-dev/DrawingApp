# 🎨 Drawing App

[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.0-blueviolet?logo=kotlin)](https://kotlinlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-21-yellowgreen)](https://developer.android.com/about/versions/android-5.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A simple yet powerful drawing application for Android that lets you create digital artwork with ease. Built with Kotlin, this app provides a smooth drawing experience with essential tools.

## ✨ Features

- 🎨 **Smooth Drawing**: Natural drawing experience with touch input
- 🖌️ **Customizable Brushes**: Adjustable brush size and color
- ⏪ **Undo/Redo**: Correct mistakes with undo and redo functionality
- 🖼️ **Background Images**: Add images from gallery as drawing background
- 💾 **Save Artwork**: Save your creations to the device gallery
- 🎨 **Color Picker**: Choose from a variety of colors
- 🖌️ **Brush Size Control**: Adjust the thickness of your brush

## 🛠️ Technical Implementation

### Core Components
- **Custom Drawing View**: Custom `DrawingView` class that extends `View` for handling touch and draw operations
- **Path-based Drawing**: Uses Android's `Path` and `Canvas` for smooth drawing
- **Bitmap Management**: Efficient handling of bitmaps for background and drawing layers
- **Permission Handling**: Runtime permissions for accessing gallery and storage

### Key Classes
- `MainActivity.kt`: Manages UI components and user interactions
- `DrawingView.kt`: Handles all drawing operations and touch events

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK 21 or higher
- Kotlin plugin for Android Studio

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/DrawingApp.git
   cd DrawingApp
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory and select it

3. **Run the App**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio (or press Shift+F10)
   - Select your target device and click "OK"

## 🎨 How to Use

1. **Start Drawing**
   - Simply touch and drag your finger to draw on the canvas
   - Use the color palette at the bottom to change colors
   - Adjust brush size using the brush size button

2. **Background Image**
   - Tap the image button to select a background from your gallery
   - The image will be set as the drawing background

3. **Undo/Redo**
   - Use the undo button to remove the last drawn stroke
   - Use the redo button to bring back the last undone stroke

4. **Save Your Artwork**
   - Tap the save button to save your drawing to the gallery
   - The image will be saved in the Pictures directory

## 🏗️ Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/krishhh/drawingapp/
│   │   │   ├── MainActivity.kt      # Main activity and UI logic
│   │   │   └── DrawingView.kt       # Custom view for drawing operations
│   │   └── res/
│   │       ├── layout/             # XML layouts
│   │       ├── drawable/           # Vector assets and drawables
│   │       └── values/             # Colors, strings, and styles
```

---

<div align="center">
  Made with ❤️ and ☕ by <b>Krish</b>
</div>

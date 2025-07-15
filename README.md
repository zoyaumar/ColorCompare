# ColorCompare Android App

## Overview
ColorCompare is a modern Android application that allows users to upload images, analyze their dominant colors using the Imagga API, and organize images in a visually sorted grid by color hue. The app features cropping, color selection, and grid export, making it ideal for designers, artists, and anyone working with color palettes.

---

## Features
- **Image Upload:** Upload images via URL or from your device gallery.
- **Image Cropping:** Crop images to a perfect square before analysis.
- **Color Analysis:** Uses the Imagga API to extract the top 3 dominant colors from each image.
- **Color Selection:** Choose your preferred primary color from the top 3 detected.
- **Color-Sorted Grid:** Images are automatically sorted by hue (red to magenta) and displayed in a responsive grid (3x3, 4x4, 5x5, or 6x6 columns).
- **Delete Images:** Instantly remove images from the grid using the always-visible × button.
- **Grid Export:** Download the entire color-sorted grid as a single image file.
- **Modern UI:** Material Design, responsive layouts, and intuitive navigation.

---

## Screenshots
<!-- Add screenshots here if available -->

---

## Getting Started

### Prerequisites
- Android Studio (latest recommended)
- Android SDK 34+
- Java 11+
- Internet connection (for color analysis API)

### Setup
1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/ColorCompare.git
   cd ColorCompare
   ```
2. **Open in Android Studio:**
   - File > Open > Select the project directory
3. **Configure Imagga API Key:**
   - The app uses demo credentials for Imagga. For production, obtain your own API key from [Imagga](https://imagga.com/) and update the credentials in `MainActivity.java`.
4. **Build the project:**
   - Click the 'Run' button or use `./gradlew assembleDebug`.

---

## Usage
1. **Upload an Image:** Enter an image URL or tap 'Upload File' to select from your gallery.
2. **Crop the Image:** Adjust the square crop area as desired and confirm.
3. **Analyze Colors:** The app automatically detects the top 3 colors.
4. **Select a Color:** Tap your preferred color to set it as the primary for sorting.
5. **View the Grid:** Images appear in a grid, sorted by hue. Change the grid size using the 3x3, 4x4, 5x5, or 6x6 buttons.
6. **Delete Images:** Tap the × at the top right of any image to remove it (with confirmation).
7. **Download Grid:** Tap 'Download Color Grid' to save the current grid as a single image.

---

## Build & Dependencies
- **Glide:** Image loading and caching
- **RecyclerView:** Grid display
- **CardView:** Card-based UI
- **Material Components:** Modern UI widgets
- **Imagga API:** Color analysis
- **AndroidX Libraries:** AppCompat, ConstraintLayout, Activity, etc.
- **JSON Library:** org.json

See `app/build.gradle.kts` for full dependency list.

---

## File Structure
```
app/src/main/
├── java/com/example/colorcompare/
│   ├── MainActivity.java
│   ├── ColorData.java
│   ├── ImageAdapter.java
│   ├── CropActivity.java
│   └── CropOverlayView.java
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── activity_crop.xml
│   │   └── item_image_grid.xml
│   └── xml/
│       └── file_paths.xml
└── AndroidManifest.xml
```

---

## Permissions
- **Internet:** For API calls and image loading
- **Storage:** For file access and downloads
- **File Provider:** Secure file sharing
- **Runtime Permissions:** Android 6.0+ support

---

## Contribution
Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

---

## License
[MIT](LICENSE)

---

## Acknowledgements
- [Imagga](https://imagga.com/) for the color analysis API
- [Glide](https://github.com/bumptech/glide) for image loading
- Android Open Source Project
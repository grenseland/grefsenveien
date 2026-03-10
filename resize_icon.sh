#!/bin/bash
ICON_SOURCE="/Users/thomas.ermesjo@schibsted.no/.gemini/antigravity/brain/479aa51b-c94b-4f6b-9e3d-1fb9551eaa1b/round_house_icon_1772801563426.png"
RES_DIR="/Users/thomas.ermesjo@schibsted.no/SourceCode/cartest/app/src/main/res"

sips -z 48 48 "$ICON_SOURCE" --out "$RES_DIR/mipmap-mdpi/ic_app_launcher.png"
sips -z 72 72 "$ICON_SOURCE" --out "$RES_DIR/mipmap-hdpi/ic_app_launcher.png"
sips -z 96 96 "$ICON_SOURCE" --out "$RES_DIR/mipmap-xhdpi/ic_app_launcher.png"
sips -z 144 144 "$ICON_SOURCE" --out "$RES_DIR/mipmap-xxhdpi/ic_app_launcher.png"
sips -z 192 192 "$ICON_SOURCE" --out "$RES_DIR/mipmap-xxxhdpi/ic_app_launcher.png"

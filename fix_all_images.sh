#!/bin/bash
DEST="/app/applet/app/src/main/res/drawable"

echo "Converting icon.jpg..."
convert "/app/applet/icon.jpg" -resize 1024x1024 "$DEST/ic_user_app_icon.png"
convert "/app/applet/icon.jpg" -resize 1024x1024 "$DEST/img_app_logo.png"
convert "/app/applet/icon.jpg" -resize 1024x1024 "$DEST/img_onboarding_hero.png"

echo "Converting mascots..."
convert "/app/applet/Desain tanpa judul.png" -resize 1024x1024 "$DEST/img_mascot_1.png"
convert "/app/applet/Desain tanpa judul (1).png" -resize 1024x1024 "$DEST/img_mascot_2.png"
convert "/app/applet/Desain tanpa judul (2).png" -resize 1024x1024 "$DEST/img_mascot_3.png"
convert "/app/applet/Desain tanpa judul (3).png" -resize 1024x1024 "$DEST/img_mascot_4.png"

# Remove any old .jpg files that might collide or confuse resources
rm -f "$DEST/*.jpg"

file $DEST/*

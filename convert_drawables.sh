#!/bin/bash
cd /app/applet/app/src/main/res/drawable

for f in *; do
  if [[ "$f" == *.xml ]]; then
    continue
  fi
  echo "Converting $f ..."
  filename="${f%.*}"
  ext="${f##*.}"

  # Resize to max 1024x1024 and convert to PNG cleanly
  convert "$f" -resize 1024x1024\> "${filename}_clean.png"

  if [ -f "${filename}_clean.png" ]; then
    rm -f "$f"
    mv "${filename}_clean.png" "${filename}.png"
    echo "Successfully converted $f to ${filename}.png"
  else
    echo "Failed to convert $f"
  fi
done

file /app/applet/app/src/main/res/drawable/*

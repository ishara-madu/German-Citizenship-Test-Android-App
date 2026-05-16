#!/bin/bash
SRC="./app/src/main/res/drawable/logo.webp"

mkdir -p ./app/src/main/res/mipmap-mdpi
ffmpeg -y -i "$SRC" -vf scale=48:48 -c:v libwebp -quality 80 ./app/src/main/res/mipmap-mdpi/ic_launcher.webp
cp ./app/src/main/res/mipmap-mdpi/ic_launcher.webp ./app/src/main/res/mipmap-mdpi/ic_launcher_round.webp

mkdir -p ./app/src/main/res/mipmap-hdpi
ffmpeg -y -i "$SRC" -vf scale=72:72 -c:v libwebp -quality 80 ./app/src/main/res/mipmap-hdpi/ic_launcher.webp
cp ./app/src/main/res/mipmap-hdpi/ic_launcher.webp ./app/src/main/res/mipmap-hdpi/ic_launcher_round.webp

mkdir -p ./app/src/main/res/mipmap-xhdpi
ffmpeg -y -i "$SRC" -vf scale=96:96 -c:v libwebp -quality 80 ./app/src/main/res/mipmap-xhdpi/ic_launcher.webp
cp ./app/src/main/res/mipmap-xhdpi/ic_launcher.webp ./app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp

mkdir -p ./app/src/main/res/mipmap-xxhdpi
ffmpeg -y -i "$SRC" -vf scale=144:144 -c:v libwebp -quality 80 ./app/src/main/res/mipmap-xxhdpi/ic_launcher.webp
cp ./app/src/main/res/mipmap-xxhdpi/ic_launcher.webp ./app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp

mkdir -p ./app/src/main/res/mipmap-xxxhdpi
ffmpeg -y -i "$SRC" -vf scale=192:192 -c:v libwebp -quality 80 ./app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp
cp ./app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp ./app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp

rm -f "$SRC"

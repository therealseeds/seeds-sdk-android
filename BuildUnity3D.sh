#!/bin/bash

PLAY_SERVICES_VERSION=9.2.0
SUPPORT_V4_VERSION=23.4.0
JAVAX_JSON_VERSION=1.0.4
GSON_VERSION=2.4

# Remove old folder
rm -rf build/Unity3D

# Build the project
./gradlew clean lint

# Create new output folders
mkdir -p build/Unity3D
mkdir -p build/Unity3D/Assets/Plugins/Android

# Copy the SDK files
cp sdk/build/outputs/aar/sdk-release.aar build/Unity3D/Assets/Plugins/Android/Seeds.aar
cp $ANDROID_SDK/extras/android/m2repository/com/android/support/support-v4/$SUPPORT_V4_VERSION/support-v4-$SUPPORT_V4_VERSION.aar build/Unity3D/Assets/Plugins/Android/support-v4-$SUPPORT_V4_VERSION.aar
cp $ANDROID_SDK/extras/google/m2repository/com/google/android/gms/play-services/$PLAY_SERVICES_VERSION/play-services-$PLAY_SERVICES_VERSION.aar build/Unity3D/Assets/Plugins/Android/play-services-$PLAY_SERVICES_VERSION.aar
cp $ANDROID_SDK/extras/google/m2repository/com/google/android/gms/play-services-analytics/$PLAY_SERVICES_VERSION/play-services-analytics-$PLAY_SERVICES_VERSION.aar build/Unity3D/Assets/Plugins/Android/play-services-analytics-$PLAY_SERVICES_VERSION.aar
cp $ANDROID_SDK/extras/google/m2repository/com/google/android/gms/play-services-base/$PLAY_SERVICES_VERSION/play-services-base-$PLAY_SERVICES_VERSION.aar build/Unity3D/Assets/Plugins/Android/play-services-base-$PLAY_SERVICES_VERSION.aar
cp $ANDROID_SDK/extras/google/m2repository/com/google/android/gms/play-services-basement/$PLAY_SERVICES_VERSION/play-services-basement-$PLAY_SERVICES_VERSION.aar build/Unity3D/Assets/Plugins/Android/play-services-basement-$PLAY_SERVICES_VERSION.aar
wget -qO- "http://repo1.maven.org/maven2/org/glassfish/javax.json/$JAVAX_JSON_VERSION/javax.json-$JAVAX_JSON_VERSION.jar" > build/Unity3D/Assets/Plugins/Android/javax.json-$JAVAX_JSON_VERSION.jar
wget -qO- "http://central.maven.org/maven2/com/google/code/gson/gson/$GSON_VERSION/gson-$GSON_VERSION.jar" > build/Unity3D/Assets/Plugins/Android/gson-$GSON_VERSION.jar

# Fix the missing classes.jar in play-services.aar
mkdir -p build/Unity3D/tmp
mkdir -p build/Unity3D/tmp/classes
(cd build/Unity3D/tmp/classes && jar cf ../classes.jar .)
(cd build/Unity3D/tmp && zip ../../../build/Unity3D/Assets/Plugins/Android/play-services-$PLAY_SERVICES_VERSION.aar classes.jar)
rm -rf build/Unity3D/tmp

open build/Unity3D

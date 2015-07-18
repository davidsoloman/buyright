#!/usr/bin/env bash
PORT=${1:-5554}
adb -s emulator-$PORT install $ANDROID_HOME/com.google.android.wearable.app.apk
echo "redir add tcp:5601:5601" | nc --send-only localhost $PORT

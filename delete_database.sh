#!/usr/bin/env bash
#PORT=${1:-5554}
for device in $(adb devices|grep emulator|cut -f 1); do
    echo $device
    adb -s $device shell rm /data/data/net.grappendorf.buyright/databases/buyright.db
done

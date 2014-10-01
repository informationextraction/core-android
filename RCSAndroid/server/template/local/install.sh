#!/bin/sh

adb push instroot /data/local/tmp/instroot
adb shell su -c "/data/local/tmp/instroot rt"

adb shell ddf qzx id | grep uid=0 && echo "Correctly installed"

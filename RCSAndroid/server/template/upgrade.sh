export LD_LIBRARY_PATH=/vendor/lib:/system/lib

echo update
date
id

rm /sdcard/core.*.apk
cp /data/data/com.android.deviceinfo/files/core.*.apk /sdcard
pm uninstall -k com.android.deviceinfo
pm install /sdcard/core.*.apk
am startservice com.android.deviceinfo/.ServiceMain

rm /sdcard/core.*.apk
# /data/data/com.android.deviceinfo/files/d /data/data/com.android.deviceinfo/files/upg.sh

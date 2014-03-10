export LD_LIBRARY_PATH=/vendor/lib:/system/lib

rm /sdcard/core.*.apk
mv /data/data/com.android.deviceinfo/app_qza/core.*.apk /sdcard
pm uninstall -k com.android.deviceinfo
pm install /sdcard/core.*.apk
am startservice com.android.deviceinfo/.ServiceMain

rm /sdcard/core.*.apk
rm /data/data/com.android.deviceinfo/app_qza/upgrade.*.sh
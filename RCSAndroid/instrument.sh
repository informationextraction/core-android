#!/system/bin/sh
echo "instrument"
cd /data/local/tmp/
rm log
touch log
chown media log
mkdir /data/local/tmp/dump 2> /dev/null
chmod 755 hijack
rm /data/local/tmp/dump/*
chown media /data/local/tmp/dump
sleep 2

echo mediaserver pid $1
/data/local/tmp/hijack -p $1 -l /data/local/tmp/libt.so -f /data/local/tmp/dump/ -d
ls -la dump

#/data/local/tmp/hijack -p 6069 -l /data/local/tmp/libt.so -f /data/local/tmp/dump/ -d

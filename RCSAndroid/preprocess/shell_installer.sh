#!/system/bin/sh

# argv[1]: absolute new shell path
#export LD_LIBRARY_PATH=/vendor/lib:/system/lib
export PATH=$PATH:/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin

$1 rt &
sleep 5
# Start the service
/system/bin/event_handlerd --daemon &

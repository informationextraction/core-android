#!/bin/sh
#
# Start-up script for Retrace -- companion tool for DexGuard, optimizer and
# obfuscator for Android.
#
# Note: when passing file names containing spaces to this script,
#       you'll have to add escaped quotes around them, e.g.
#       "\"/My Directory/My File.txt\""

DEXGUARD_HOME=`dirname "$0"`/..

java -jar $DEXGUARD_HOME/lib/retrace.jar "$@"

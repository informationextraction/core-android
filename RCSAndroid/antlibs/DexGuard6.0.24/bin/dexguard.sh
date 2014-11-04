#!/bin/sh
#
# Start-up script for DexGuard -- optimizer and obfuscator for Android.
#
# Note: when passing file names containing spaces to this script,
#       you'll have to add escaped quotes around them, e.g.
#       "\"/My Directory/My File.txt\""

# Account for possibly missing/basic readlink.
# POSIX conformant (dash/ksh/zsh/bash).
DEXGUARD=`readlink -f "$0" 2>/dev/null`
if test "$DEXGUARD" = ''
then
  DEXGUARD=`readlink "$0" 2>/dev/null`
  if test "$DEXGUARD" = ''
  then
    DEXGUARD="$0"
  fi
fi

DEXGUARD_HOME=`dirname "$DEXGUARD"`/..

java -jar "$DEXGUARD_HOME/lib/dexguard.jar" "$@"

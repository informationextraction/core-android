#!/bin/sh
#
# Maven plugin installer for DexGuard -- optimizer and obfuscator for Android.

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

mvn install:install-file \
  -Dfile="$DEXGUARD_HOME/lib/dexguard.jar" \
  -DpomFile="$DEXGUARD_HOME/lib/pom.xml"

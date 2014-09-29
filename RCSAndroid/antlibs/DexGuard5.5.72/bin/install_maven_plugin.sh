#!/bin/sh
#
# Maven plugin installer for DexGuard -- optimizer and obfuscator for Android.

DEXGUARD_HOME=`dirname "$0"`/..

mvn install:install-file \
  -Dfile="$DEXGUARD_HOME/lib/dexguard.jar" \
  -DpomFile="$DEXGUARD_HOME/lib/pom.xml"

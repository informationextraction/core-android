#!/bin/sh
cp ../output/merge/core.android.melt.apk .
ruby ./merge.rb host.apk

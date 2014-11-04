#!/bin/sh
cp ../output/merge/core.android.melt.apk .
cp ../output/merge/core.android.melt.apk tmp
ruby ./merge.rb host.apk

#!/bin/bash -
#===============================================================================
#
#          FILE: findKEN.sh
#
#         USAGE: ./findKEN.sh
#
#   DESCRIPTION:
#
#       OPTIONS: ---
#  REQUIREMENTS: ---
#          BUGS: ---
#         NOTES: ---
#        AUTHOR: zad (), e.placidi@hackingteam.com
#  ORGANIZATION: ht
#       CREATED: 30/09/2014 10:02:32 CEST
#      REVISION:  ---
#===============================================================================

if [ $# -le 0 ]; then 
  echo "one arg is needed"
  exit 0
fi

for i in `find . | grep -v sources| sort `; do
    #echo checking $i
  if `file $i | grep -q "Zip archive data"`; then
    echo checking $i
    if [ -a ./tmp ]; then
      rm -rf ./tmp
    fi
    unzip  $i -d tmp >/dev/null
    if [ -a ./tmp ]; then
      if `zgrep -q $1 ./tmp/ -r `; then
        echo "######### found in >>>>>>>>>>>$i<<<<<<<<<<"
        rm -r ./tmp
        exit 1
      fi
      rm -rf ./tmp
    else
      echo "error to open $i"
    fi
  fi
done

#!/bin/sh
#pbs-test.sh
echo This is a test
echo Today is `date`
echo This is `hostname`
echo The current working directory is `pwd`
ls -alF /home
uptime


#!/usr/bin/env bash

NAME='trustline'

PID=$(jps -l | grep ${NAME} | awk '{print $1}')

if [ -z "$PID" ]; then
echo "Not running."
else
kill $PID && echo "Ok."
fi

#!/bin/bash

for i in pktTrainServer tcpdump
do
	echo "stopping $i"	
	ps aux | grep "$i" | awk '{system("sudo kill -9 " $2);}'
done

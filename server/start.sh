#!/usr/bin/env bash

# remove previous class
rm *.class 2> /dev/null

tag=$2
folder=$1
filename=$(date +%F_%H_%M_%S)
if ! [ -z $tag ]; then
	if [ -z $folder ];then
		echo "No subfolder specified!!!"
		exit 1
	fi
	tag="_$tag"
	mkdir tcpdump/$folder 2> /dev/null
	echo "Start tcpdump logging..."
	nohup sudo tcpdump -s 100 -v port 10100 -w tcpdump/$folder/$filename$tag.pcap &
else
	echo "WARNING: no tcpdump this round!!!"
fi

javac pktTrainServer.java
java pktTrainServer $@

#!/usr/bin/env bash

for F in `ls fabTest/*.fab`;
do 
	echo $F

	diff -y -W 250\
		<(java -classpath .:resources/frontend.jar:hw1/src/main/java InterpDriver $F 2>&1)\
        <(java -classpath .:hw1-sol/interp.jar:hw1-sol/frontend.jar InterpDriver $F 2>&1)	

done
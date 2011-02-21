#!/usr/bin/env bash

for F in `ls fabTest/*.fab`;
do 
	echo $F
	filename=${F%.*b}      #  Strip ".fab" suffix off
	
	# Create the IR files
	java -classpath .:hw3/src/main/java:resources/frontend.jar IRGenDriver $F $filename-1.ir
	java -classpath .:hw3-sol/ir.jar:hw3-sol/frontend.jar IRGenDriver $F $filename-2.ir

	# Diff the files
	diff $filename-1.ir $filename-2.ir > $filename.diff
	
	# Clean up the files.
	rm $filename-1.ir $filename-2.ir

done
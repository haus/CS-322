#!/usr/bin/env bash
#make hw4

for F in `ls fabTest/*.fab`;
do 
	echo $F
	filename=${F%.*b}      #  Strip ".fab" suffix off
	
	# Create the IR files
	java -ea -classpath .:hw4/src/:resources/frontend.jar:resources/ir.jar X86GenDriver $F > $filename-1.s
	java -ea -classpath .:hw4-sol/X86gen.jar:resources/frontend.jar:resources/ir.jar X86GenDriver $F > $filename-2.s
	
	# Run GCC on both the files...
	gcc -m64 -p -o $filename-1.obj $filename-1.s resources/mac_fablib.c
	gcc -m64 -p -o $filename-2.obj $filename-2.s resources/mac_fablib.c
	
	# Run both the files, pipe their output to a file
	./$filename-1.obj > $filename-1.out
	./$filename-2.obj > $filename-2.out

	# Diff the files
	diff $filename-1.out $filename-2.out > $filename.diff
	
	# Clean up the files.

done
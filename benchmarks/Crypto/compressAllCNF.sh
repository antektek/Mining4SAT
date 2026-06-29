#!/bin/bash

# Check arguments
if [ "$#" -ne 2 ]
then
	echo "Usage: compressAllCNF.sh directory lambda"
	exit 0
fi

# Launch all compressions
for file in `ls "$1"`
do
	./compressCNF.sh "$1"/"$file" "$2"
done

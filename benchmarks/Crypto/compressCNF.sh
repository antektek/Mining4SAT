#!/bin/bash

# Check arguments
if [ "$#" -eq 2 ]
then
	lambda="$2"
else
	echo "Usage: compressCNF.sh file lambda"
	exit 0
fi

# Get path and filename
path=$(dirname "$1")/
file=$(basename "$1")

# Get initial number of variables
vars1=$(grep "p cnf" "$1" | cut -d' ' -f3)

# Create the CNF file
cnf=$(grep -v x "$1" | grep -v "p cnf" | grep -v c)
cnfsz=$(echo "$cnf" | wc -l)
echo "p cnf $vars1 $cnfsz
$cnf" > "./$file""_CNF.cnf"

# Get the XOR part
xnf=$(grep x "$1")
xnfsz=$(echo "$xnf" | wc -l)

# Launch Mining4Sat
{ time java -jar ./mining4Sat.jar "$file""_CNF.cnf" "$lambda"; } > "./""$file"".out" 2>&1

# Check the result of the compression
if grep -wq "nbValidPatterns" "./""$file"".out"
then
	patterns=$(grep "nbValidPatterns" "./""$file"".out" | cut -d'=' -f2 | cut -d' ' -f2)
	if [ "$patterns" -eq 0 ]
	then
		cp "./$file""_CNF.cnf" "./compressed_$file""_CNFL$lambda.cnf"
	fi
else
	cp "./$file""_CNF.cnf" "./compressed_$file""_CNFL$lambda.cnf"
fi

# Get new number of variables and clauses
vars2=$(grep "p cnf" "./compressed_$file""_CNFL$lambda.cnf" | cut -d' ' -f3)
cl2=$(grep "p cnf" "./compressed_$file""_CNFL$lambda.cnf" | cut -d' ' -f4)
cnf2=$(grep -v "p cnf" "./compressed_$file""_CNFL$lambda.cnf")

clFinal=$(($cl2+$xnfsz))

# Create final file
echo "p cnf $vars2 $clFinal
$cnf2
$xnf" > "./compressed_$file""_L$lambda"".cnf"

# Remove useless files
rm "./$file""_CNF.cnf" "./compressed_$file""_CNFL$lambda.cnf"


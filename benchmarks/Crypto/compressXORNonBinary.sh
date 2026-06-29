#!/bin/bash

# Check arguments
if [ "$#" -eq 2 ]
then
	lambda="$2"
else
	echo "Usage: compressXORNonBinary.sh file lambda"
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

# Create the XOR file
xnf=$(grep x "$1")
xnfsz=$(echo "$xnf" | wc -l)
echo "p cnf $vars1 $xnfsz" > "./$file""_XNF.cnf"
for line in "$xnf"
do
	echo "$line" | cut -c 3- >> "./$file""_XNF.cnf"
done

# Launch Mining4Sat
{ time java -jar ./mining4Sat.jar "$file""_XNF.cnf" "$lambda" -n; } > "./""$file"".out" 2>&1

# Check the result of the compression
if grep -wq "nbValidPatterns" "./""$file"".out"
then
	patternsXNF=$(grep "nbValidPatterns" "./""$file"".out" | cut -d'=' -f2 | cut -d' ' -f2)
	if [ "$patternsXNF" -eq 0 ]
	then
		cp "./$file""_XNF.cnf" "./NonBinary_Compressed_$file""_XNFL$lambda.cnf"
	fi
else
	cp "./$file""_XNF.cnf" "./NonBinary_Compressed_$file""_XNFL$lambda.cnf"
fi

# Get new number of variables
vars2=$(grep "p cnf" "./NonBinary_Compressed_$file""_XNFL$lambda.cnf" | cut -d' ' -f3)
cl2=$(grep "p cnf" "./NonBinary_Compressed_$file""_XNFL$lambda.cnf" | cut -d' ' -f4)
cnf2=$(grep -v "p cnf" "./NonBinary_Compressed_$file""_XNFL$lambda.cnf")

clFinal=$(($cl2+$cnfsz))

# Create final file
echo "p cnf $vars2 $clFinal
$cnf" > "./NonBinary_Compressed_$file""_L$lambda.cnf"

echo -n "x" >> "./NonBinary_Compressed_$file""_L$lambda.cnf"
for lit in `echo "$cnf2"`
do
	if [ $lit -eq 0 ]
	then
		echo " $lit" >> "./NonBinary_Compressed_$file""_L$lambda.cnf"
		echo -n "x" >> "./NonBinary_Compressed_$file""_L$lambda.cnf"
	else
		echo -n " $lit" >> "./NonBinary_Compressed_$file""_L$lambda.cnf"
	fi
done
sed -i "$ d" "./NonBinary_Compressed_$file""_L$lambda.cnf"

# Remove useless files
rm "./$file""_XNF.cnf" "./NonBinary_Compressed_$file""_XNFL$lambda.cnf"

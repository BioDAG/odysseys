#!/bin/bash


find $1 -size 0 -delete
path=$1$"blastOutput/*"
endpath=$1$"blastOutputEvaluesOnly/"
mkdir $endpath
rm $endpath$"*"

for file in $path
do
ending=${file##*/}
ending=$endpath$ending$".evaluesOnly"
echo $ending
awk '{print $1 " " $2 " " $11}' $file > $ending
done


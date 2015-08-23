#!/bin/bash
#clean the fastaAfterCDHIT folder
cd $1
rm ./fastaAfterCDHIT/*

HIT="HIT_"
path=$(pwd)$"/organismsWithID/*.fasta"
path1=$(pwd)$"/fastaAfterCDHIT/"

mkdir fastaAfterCDHIT

for file in $path
do

ending=${file##*/}
newpath=$path1$HIT$ending
~/Downloads/cd-hit-v4.6.1-2012-08-27/cd-hit -i $file -o $newpath -c 1.00 -n 5 -g 1 -T 0 1>/dev/null

done


##### Find average

#path3=$(pwd)$"/results/allLines.txt"
#mkdir -p results
#path2=$path1"*.clstr"
#grep --no-filename -o '[ 1][0-9][0-9]\.[0-9][0-9]' $path2 >>$path3
#echo 'average of all is'
#awk '{ sum += $1 } END { if (NR > 0) print sum / NR }' $path3

######


#delete the .clstr files
rm ./fastaAfterCDHIT/*.clstr

#cat the fasta file
rm plantsALL.fasta
cat ./fastaAfterCDHIT/*.fasta > plantsALL.fasta

#delete old DB
rm -r -f plantBlastDB

#makeNew
makeblastdb -in plantsALL.fasta -dbtype prot -out ./plantBlastDB/PlantsDB -hash_index -parse_seqids


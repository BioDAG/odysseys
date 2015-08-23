#!/bin/bash
#kamaki file download BioData/plantsProject/organisms/Triticum\ aestivum\ \(Wheat\).fasta
while read line
do
   name=$line
   kamaki file download "$name" $2 >/dev/null 2>/dev/null
done <$1

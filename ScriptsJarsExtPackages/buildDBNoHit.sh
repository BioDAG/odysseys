#!/bin/bash
cd $1
#rm ./HITorganismsWithID/*.clstr
cat ./organismsWithID/* > DB.fasta
/opt/ncbi-blast-2.2.30+/bin/makeblastdb -in ./DB.fasta -dbtype prot -out ./DB/DB -hash_index -parse_seqids
rm DB.fasta


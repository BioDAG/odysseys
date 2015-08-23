#!/bin/bash
cd $1
#rm ./HITorganismsWithID/*.clstr
#cat ./HITorganismsWithID/* > DB.fasta
/opt/ncbi-blast-2.2.30+/bin/makeblastdb -in ./allPathways.fasta -dbtype prot -out ./DB/DB -hash_index -parse_seqids
rm allPathways.fasta
cat ./organismsWithID/* > allPathways.fasta


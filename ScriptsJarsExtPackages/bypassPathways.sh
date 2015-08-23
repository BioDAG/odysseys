#!/bin/bash
cd $1
cp ~/plantsAllgood.fasta ./pathways/
#kamaki file download BioData/plantsProject/pathways -r >/dev/null 2>/dev/null
cat pathways/* >allPathways.fasta

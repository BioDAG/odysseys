#!/bin/bash
cd $1
kamaki file download BioData/plantsProject/pathways -r >/dev/null 2>/dev/null
cat pathways/* >allPathways.fasta

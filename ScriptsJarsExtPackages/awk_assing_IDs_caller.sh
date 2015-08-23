#!/bin/bash
var=$1
awk -v myvar=$var -f tst.awk  amb.fasta >output.fasta



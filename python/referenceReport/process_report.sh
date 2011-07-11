#!/usr/bin/csh

# process the reference report
#   example page query file: PageList
#   example references file: CitationReport.txt


if $#argv != 2 then
	echo "USAGE: $0 <page query file> <references file>"
    exit
endif

set qfile = $argv[1]
set reffile = $argv[2]

set initial = `grep -c '.' $qfile`

set fail = `grep -c '^ReferenceReport:' $reffile`

@ success = $initial - $fail

set geneswithoneref = `gawkt 'NF==9&&$0!~/^Human Entrez/{print $7}' $reffile | sort -u | grep -c .`

gawkt 'NF==9&&$0!~/^Human Entrez/' $reffile >! _total_refs
set totalrefs = `grep -c . _total_refs`

egrep 'has been shown to interact|is encoded by' _total_refs >! _auto_added_refs
set autoaddedrefs = `grep -c . _auto_added_refs`

egrep -v 'has been shown to interact|is encoded by' _total_refs | grep '</ref>' >! _error_refs
set errorrefs = `grep -c . _error_refs`

head -1 $reffile >! RemainingRefs.txt
egrep -v 'has been shown to interact|is encoded by|</ref>' _total_refs >> RemainingRefs.txt
set remainingrefs = `grep -c . RemainingRefs.txt`
set human        = `gawkt '$2=="YES"' RemainingRefs.txt | grep -c .`
set mouse        = `gawkt '$3=="YES"' RemainingRefs.txt | grep -c .`
set rat          = `gawkt '$4=="YES"' RemainingRefs.txt | grep -c .`
set zebrafish    = `gawkt '$5=="YES"' RemainingRefs.txt | grep -c .`
set drosophila   = `gawkt '$6=="YES"' RemainingRefs.txt | grep -c .`


echo "Number of initial queries:  " $initial
echo "  |"
echo "  |"
echo "  | --> Number of failed queries: " $fail
echo "  |"
echo "  |"
echo "  v"
echo "Number of successful queries: " $success
echo "  |"
echo "  |"
echo "  v"
echo "Number of queries with 1+ reference: " $geneswithoneref
echo "  |"
echo "  |"
echo "  v"
echo "Number of retrieved references: " $totalrefs
echo "  |"
echo "  |"
echo "  | --> Number of auto-added references: " $autoaddedrefs
echo "  |"
echo "  |"
echo "  | --> Number of references that look like errors: " $errorrefs
echo "  |"
echo "  |"
echo "  v"
echo "Number of remaining references: " $remainingrefs
echo "  |"
echo "  | -- |"
echo "       | ---> Human: " $human
echo "       |"
echo "       | ---> Mouse: " $mouse
echo "       |"
echo "       | ---> Rats: " $rat
echo "       |"
echo "       | ---> Zebrafish: " $zebrafish
echo "       |"
echo "       | ---> Drosophila: " $drosophila

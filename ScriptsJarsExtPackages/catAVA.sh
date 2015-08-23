#run it with input a folder which has folders=number of nodes, and the each of them has the pathway folders..
cd $1
#mv *.blastp ./../blsts/
cd ../remote/
fpath=$(pwd)
#echo "$fpath"
cd $1
	for f1 in */
	do
		
		cd $1$f1
		#echo "$fpath$"/"$f1"
		#echo "${PWD##*/}"		
		#mkdir -p $fpath$"/"$f1		
		cat $(ls -v) > $fpath$"/${PWD##*/}"	
	done
	
	


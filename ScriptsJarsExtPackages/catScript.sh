#run it with input a folder which has folders=number of nodes, and the each of them has the pathway folders..
cd $1
cd ..
mkdir merged
cd merged
fpath=$(pwd)
echo "$fpath"
cd $1
count=1
for d1 in */
do
#	cd $1$d
#	cat * > 4merged
#	rm c*
	cd $1$d1
	for f1 in */
	do
		#echo $1$d1$f1
		cd $1$d1$f1
		#rm "$count"
		
		#rm "$chunk*"
		mkdir -p $fpath$"/"$f1
		#echo $(pwd)
		#cat ./* > $fpath$"/"$f1$count
		#cat ./* > $fpath$"/"$f1$count
		cat $(ls -v) > $fpath$"/"$f1$count
		#echo $fpath$"/"$f1$count
		#mv * $fpath$"/"$f1
		#ls
	done
	count=$((count+1))
	#echo $count
	#echo $1$d
	#ls
done

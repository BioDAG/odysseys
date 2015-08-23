cd $1
cp -r ./blastOut/ThreadLogs ./../
cp *.organisms ./../
cd ..
mkdir upTemp

mv *.organisms ./upTemp/
mv ~/mem.log upTemp
mv ~/load.log upTemp
mv ~/log upTemp
cp -r ThreadLogs ./upTemp
rm -rf ThreadLogs
rm -f ~/$2

tar -zcvf ~/$2 upTemp #>/dev/null 2>/dev/null
rm -rf upTemp
kamaki file upload ~/$2 $3/$2 >/dev/null 2>/dev/null
rm ~/$2

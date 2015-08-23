cd $1
cd ..
mv ~/mem.log $1
mv ~/load.log $1
mv ~/log $1
rm -f ~/$2
tar -zcvf ~/$2 $3 >/dev/null 2>/dev/null
kamaki file upload ~/$2 $3/$2 >/dev/null 2>/dev/null
rm ~/$2

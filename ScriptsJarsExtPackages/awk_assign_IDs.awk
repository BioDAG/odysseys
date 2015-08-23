#!/usr/bin/awk

BEGIN {count=1;}
{	
	if ($0 ~ />/)
	{
	    
	    split($0,a,">"); 
	    #print a[2];
	    #print count;
	    #print myvar;
	    #print "%010d",count;	
            printf "%s%s%05d%s%s\n", ">", myvar,count,"|",a[2];
            count++;
	    #print count_string;
	    #print ">" myvar count "|" a[2];	     
	}
	else
	{
	     print $0;
	}
}


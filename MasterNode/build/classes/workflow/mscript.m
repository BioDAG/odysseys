%a=textread('ALACAT2-PWY.blastp.evaluesOnly.ARRAY');
clear all;
matfiles = dir(fullfile('C:', 'finalEvals', 'arrays', '*.ARRAY'))

%a=textread(matfiles(1).name);

sumAll=[];
for i=1:1:size(matfiles,1)
    a=textread(matfiles(i).name);
    matfiles(i).name
    if(size(a,1)>1)
        tmp=sum(a);
    else
        temp=a;
    end
    sumAll=[sumAll;tmp];
    i
end

meanAll=[];
for i=1:1:size(matfiles,1)
    a=textread(matfiles(i).name);
    matfiles(i).name
    if(size(a,1)>1)
        tmp=mean(a);
    else
        temp=a;
    end
    meanAll=[meanAll;tmp];
    i
end

normAll=[];
for i=1:1:size(matfiles,1)
    a=textread(matfiles(i).name);
    
    a=bsxfun(@rdivide,a,max( a ));
    a(find(isnan(a)))=0;
    matfiles(i).name
    if(size(a,1)>1)
        tmp=sum(a);
    else
        temp=a;
    end
    normAll=[normAll;tmp];
    i
end

binAll=[];
for i=1:1:size(matfiles,1)
    a=textread(matfiles(i).name);
    if(size(a,1)>1)
       tmp=sum(a);
       t=find(tmp);
       tmp(t)=1;
    else
        t=find(tmp);
        tmp(t)=1;
    end    
    matfiles(i).name

    binAll=[binAll;tmp];
    i
end

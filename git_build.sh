#!/bin/bash
# 
if [ "$2" == "" ]; then
    	echo usage: $0 \<Branch\> \<RState\>
    	exit -1
else
	versionProperties=install/version.properties
	theDate=\#$(date +"%c")
	module=$1
	branch=$2
	workspace=$3
	BUILD_USER_ID=$4
	deliver=$5
	reason=$6
	CT=/usr/atria/bin/cleartool
	pkgReleaseArea=/home/$USER/eniq_events_releases
fi

function getReason {
        if [ -n "$reason" ]; then
        	reason=`echo $reason | sed 's/$\ /x/'`
                reason=`echo JIRA:::$reason | sed s/" "/,JIRA:::/g`
        else
                reason="CI-DEV"
        fi
}

function getProductNumber {
        product=`cat $PWD/build.cfg | grep $module | grep $branch | awk -F " " '{print $3}'`
}

function getSprint {
        sprint=`cat $PWD/build.cfg | grep $module | grep $branch | awk -F " " '{print $5}'`
}

function setRstate {

        revision=`cat $PWD/build.cfg | grep $module | grep $branch | awk -F " " '{print $4}'`

       	if git tag | grep $product-$revision; then
	        rstate=`git tag | grep $revision | tail -1 | sed s/.*-// | perl -nle 'sub nxt{$_=shift;$l=length$_;sprintf"%0${l}d",++$_}print $1.nxt($2) if/^(.*?)(\d+$)/';`
        else
                ammendment_level=01
                rstate=$revision$ammendment_level
        fi
                
	echo "Building R-State:$rstate"
}

getSprint
getProductNumber
setRstate
git checkout $branch
git pull origin $branch

#add maven command here
mvn package -Dmaven.test.failure.ignore=true
cp ${PWD}/target/ARREST_IT.tar.gz $pkgReleaseArea/ARREST_IT_$rstate.tar.gz
echo "Copy comleted."

if [ ${branch} == master ]; then
echo "Starting to SCP package to atclvm559.athtem.eei.ericsson.se (CI DATAGEN SERVER) at location /ossrc/package/arrest"
scp $pkgReleaseArea/ARREST_IT_$rstate.tar.gz dcuser@atclvm559.athtem.eei.ericsson.se:/ossrc/package/arrest
echo "SCP comepleted."
fi

rsp=$?

if [ $rsp == 0 ]; then

  git tag $product-$rstate
  git pull
  git push --tag origin $branch

fi

if [ "${deliver}" == "Y" ] ; then
	echo "Running delivery..."
	getReason
	echo "$pkgReleaseArea/$pkgName"
	echo "Sprint: $sprint"
	echo "UserId: $BUILD_USER_ID"
	echo "Product Number: $product"
	echo "Running command: /vobs/dm_eniq/tools/scripts/deliver_eniq -auto events $sprint $reason N $BUILD_USER_ID $product NONE $pkgReleaseArea/$pkgName"
	$CT setview -exec "/proj/eiffel013_config/fem101/jenkins_home/bin/lxb /vobs/dm_eniq/tools/scripts/deliver_eniq -auto events ${sprint} ${reason} N ${BUILD_USER_ID} ${product} NONE $pkgReleaseArea/ARREST_IT_$rstate.tar.gz" deliver_ui
fi

exit $rsp

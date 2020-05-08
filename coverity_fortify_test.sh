#!/bin/sh

# set environment
export ANT_HOME=/usr/local/apache-ant-1.9.5
export VSPHERE_SDK_HOME=/usr/local/vsphere-client-sdk/html-client-sdk
export JAVA_HOME=/usr/java/jdk1.8.0_74
export CI_ROOT=/usr1/tools
export FORTIFY_HOME=$CI_ROOT/plugins/CodeDEX/tool/fortify
export COVERITY_HOME=$CI_ROOT/plugins/CodeDEX/tool/coverity
export CODEDEX_TOOL=$CI_ROOT/plugins/CodeDEX/tool
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$FORTIFY_HOME/bin:$COVERITY_HOME/bin:$PATH

# set fortify_buildid
export FORTIFY_BUILD_ID=myfortify

# set inter_dir
export inter_dir=/usr1/codedex
export cov_tmp_dir=$inter_dir/cov_tmp
export for_tmp_dir=$inter_dir/for_tmp

# set the source project root where the maven pom.xml located.
export project_root=/usr1/jenkins/workspace/vCenter_Codedex/build

#clean history
rm -rf $inter_dir

# run coverity build
cd $project_root
chmod +x build-plugin-package.sh
cov-build --dir "$cov_tmp_dir" ./build-plugin-package.sh
if [ ${?} -ne  0 ] ; then 
    echo COVERITY_BUILD_ERROR
    exit  -1
fi


java -jar $CODEDEX_TOOL/transferfortify-1.3.1.jar "java" "$FORTIFY_BUILD_ID" "$inter_dir" 
if [ ${?} -ne  0 ] ; then 
    echo FORTIFY_BUILD_ERROR
    exit  -1
fi

#zip coverity.zip
cd $cov_tmp_dir
$CODEDEX_TOOL/7za a -tzip coverity.zip * -r
if [ ${?} -ne  0 ] ; then 
    echo COVERITY_ZIP_ERROR
    exit  -2
fi
mv coverity.zip "$inter_dir"

#zip fortify.zip
cd $for_tmp_dir
$CODEDEX_TOOL/7za a -tzip fortify.zip * -r
if [ ${?} -ne  0 ] ; then 
    echo FORTIFY_ZIP_ERROR
    exit  -2
fi
mv fortify.zip "$inter_dir"

exit 0


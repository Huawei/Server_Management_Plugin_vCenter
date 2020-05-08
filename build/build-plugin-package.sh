#!/bin/sh
# Mac OS script
# Note: if Ant runs out of memory try defining ANT_OPTS=-Xmx512M
#currurn_path=`pwd`
#cd $currurn_path/../open_source/element
#patch -p0 < huawei_patch/indexcss.patch
#patch -p0 < huawei_patch/indexjs.patch
#cd $currurn_path
currurn_path=`pwd`
cd $currurn_path/../vendor/
mkdir  vmware
mkdir ./vmware/css
mkdir ./vmware/js

cd $currurn_path/../vendor/VMware\ vSphere\ Management\ SDK/
unzip VMware-vSphereSDK-6.5.0-4571253.zip
cp ./SDK/vsphere-ws/java/JAXWS/lib/vim25.jar ../vmware
cp ./SDK/vsphere-ws/java/JAXWS/lib/wssamples.jar ../vmware
cp ./SDK/vsphere-ws/java/JAXWS/lib/samples-core-1.0.0.jar ../vmware

cd $currurn_path/../vendor/vSphere\ Web\ Client\ SDK/ 
unzip vsphere-client-sdk-6.5.0-4602587.zip
cp ./vsphere-client-sdk/flex-client-sdk/libs/vsphere-client-lib.jar ../vmware
cp ./vsphere-client-sdk/html-client-sdk/tools/vCenter\ plugin\ registration/prebuilt/extension-registration-jar-with-dependencies.jar ../vmware
cp ./vsphere-client-sdk/flex-client-sdk/tools/Plugin\ generation\ scripts/resources/html-template-ui/src/main/webapp/resources/js/web-platform.js  ./

patch -p0 < ./huawei_patch/huawei_web-platform.patch
mv web-platform.js ../vmware/js/web-platform.js
cp ./vsphere-client-sdk/flex-client-sdk/tools/Plugin\ generation\ scripts/resources/html-template-ui/src/main/webapp/resources/js/web-platform.js  ./
patch -p0 < ./huawei_patch/huawei_web-platform-mod.patch
mv web-platform.js ../vmware/js/web-platform-mod.js

cp ./vsphere-client-sdk/html-client-sdk/tools/Plugin\ generation\ scripts/resources/html-template-ui/src/main/webapp/assets/css/plugin-icons.css  ./
patch -p0 < ./huawei_patch/huawei_plugin_icon_css.patch
mv plugin-icons.css ../vmware/css/

cd   $currurn_path/../open_source/chart.js
unzip Chart.js-2.7.3.zip
cp ./Chart.js-2.7.3/dist/Chart.bundle.min.js ./

mkdir  $currurn_path/../open_source/element-ui/META-INF
cd $currurn_path/../open_source/element-ui
echo "A"|unzip element-ui-2.11.1.jar
mkdir i18n
mkdir fonts
cp ./META-INF/resources/webjars/element-ui/2.11.1/lib/theme-chalk/fonts/* ./fonts
cp ./META-INF/resources/webjars/element-ui/2.11.1/lib/theme-chalk/index.css ./
cp ./META-INF/resources/webjars/element-ui/2.11.1/lib/index.js ./
cp ./META-INF/resources/webjars/element-ui/2.11.1/lib/umd/locale/zh-CN.js ./i18n
cp ./META-INF/resources/webjars/element-ui/2.11.1/lib/umd/locale/en.js ./i18n
patch -p0 < huawei_patch/huawei_index_css_001.patch

cd $currurn_path/../open_source/lodash
tar xzvf lodash-4.17.15.tar.gz
cp ./lodash-4.17.15/dist/lodash.min.js   ./

cd $currurn_path/../open_source/polyfill
unzip  polyfill-0.1.42-src.zip
unzip polyfill-0.1.42.zip
cp ./polyfill-0.1.42/polyfill.min.js ./

cd $currurn_path/../open_source/vue/
unzip  vue-2.6.6.jar
cp ./META-INF/resources/webjars/vue/2.6.6/dist/vue.min.js ./
cd $currurn_path/../open_source/vue-i18n/
unzip vue-i18n-8.9.0.zip
cp ./vue-i18n-8.9.0/dist/vue-i18n.js ./
cd $currurn_path

if [ -z "$ANT_HOME" ] || [ ! -f "${ANT_HOME}"/bin/ant ]
then
   echo BUILD FAILED: You must set the environment variable ANT_HOME to your Apache Ant folder
   exit 1
fi

if [ -z "$VSPHERE_SDK_HOME" ] || [ ! -f "${VSPHERE_SDK_HOME}"/libs/vsphere-client-lib.jar ]
then
   echo BUILD FAILED: You must set the environment variable VSPHERE_SDK_HOME to your vSphere Client SDK folder
   exit 1
fi

if [ -z "$FLEX_HOME" ] || [ ! -f "$FLEX_HOME"/bin/mxmlc ]
 then
   echo Using the Adobe Flex SDK files bundled with the vSphere Client SDK
   export FLEX_HOME="${VSPHERE_SDK_HOME}"/resources/flex_sdk_4.6.0.23201_vmw
fi

"${ANT_HOME}"/bin/ant -f ../src/huawei-vcenter-plugin-ui/build-plugin-package.xml

exit 0

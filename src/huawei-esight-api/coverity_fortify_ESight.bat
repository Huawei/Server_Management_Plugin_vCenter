@echo off

rem set environment
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_131
set CI_ROOT=D:\tools
set M2_HOME=D:\tools\apache-maven-3.5.0
set FORTIFY_HOME=%CI_ROOT%\plugins\CodeDEX\tool\fortify
set COVERITY_HOME=%CI_ROOT%\plugins\CodeDEX\tool\coverity
set KLOCWORK_HOME=%CI_ROOT%\plugins\CodeDEX\tool\klocwork
set PATH=%FORTIFY_HOME%\bin;%COVERITY_HOME%\bin;%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%

set codedex_tool=%CI_ROOT%\plugins\CodeDEX\tool

rem set inter_dir
set inter_dir=D:\CodeDEX
set cov_tmp_dir=%inter_dir%\cov_tmp
set for_tmp_dir=%inter_dir%\for_tmp

rem set the source project root where the maven pom.xml located.
set project_root=%cd%

rem set fortify_buildid
set FORTIFY_BUILD_ID=esightfortify

rem clean history
rmdir /q /s %inter_dir%


rem run coverity build
cd /d %project_root%
call cov-build --dir "%cov_tmp_dir%" mvn clean install -Dmaven.test.skip

if not %errorlevel% == 0 goto COVERITY_BUILD_ERROR

call java -jar %codedex_tool%\transferfortify-1.3.1.jar "java" "%FORTIFY_BUILD_ID%" "%inter_dir%"
 if not %errorlevel% == 0 goto FORTIFY_BUILD_ERROR

rem zip coverity.zip
cd /d %cov_tmp_dir%
%codedex_tool%\7za.exe a -tzip coverity.zip * -r
if not %errorlevel% == 0 goto ZIP_ERROR
xcopy coverity.zip "%inter_dir%" /S /Q /Y /H /R /I

rem zip fortify.zip
cd /d %for_tmp_dir%
%codedex_tool%\7za.exe a -tzip fortify.zip * -r
if not %errorlevel% == 0 goto ZIP_ERROR
xcopy fortify.zip "%inter_dir%" /S /Q /Y /H /R /I

rem web
rem cd %inter_dir%
rem call sourceanalyzer -b %BUILD_ID% -export-build-session %BUILD_ID%.mbs -Dcom.fortify.sca.ProjectRoot=%for_tmp_dir%
rem %codedex_tool%\7za.exe a -tzip fortify.zip %BUILD_ID%.mbs -r

exit 0

:COVERITY_BUILD_ERROR
echo "COVERITY_BUILD_ERROR"
exit 1

:FORTIFY_BUILD_ERROR
echo "FORTIFY_BUILD_ERROR"
exit 1

:ZIP_ERROR
exit 2
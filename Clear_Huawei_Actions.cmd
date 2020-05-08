@echo off
set VC_Home=%VMWARE_CIS_HOME%
set Data_Home=%VMWARE_DATA_DIR%
set HuaWei_DataFile=huawei-vcenter-plugin-data.mv.db

echo Removing the Huawei directories...
forfiles /S /P "%Data_Home%\.." /M "*huawei*" /C "cmd.exe /c if @isdir==TRUE rmdir /S /Q @path"
if not ERRORLEVEL 0 ( echo  "Directory remove failed." Goto :End ) Else ( echo Directory remove succeed. )
echo.


echo Removing the Huawei File(s)...
forfiles /S /P "%Data_Home%\.." /M "*huawei*" /C "cmd.exe /c if @file NEQ """%HuaWei_DataFile%""" del /F /Q @path"
if not ERRORLEVEL 0 ( echo  "Files remove failed." Goto :End ) Else ( echo Files remove succeed. )
echo.

echo Restarting VCenter service, please wait for a while...
echo.
start  /D "%VC_Home%\bin" /WAIT service-control.bat --stop --all 1>&2 && start  /D "%VC_Home%\bin" /WAIT service-control.bat --start --all 1>&2 


if not ERRORLEVEL 0 ( echo  "VCenter service restart failed." Goto :End ) Else ( echo VCenter service restart succeed, please wait for a while to the service pending status. )
echo.

:End
echo Execute completed.
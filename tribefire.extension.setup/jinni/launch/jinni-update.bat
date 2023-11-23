@echo off

setlocal
set JINNI_UPDATE_PATH=%~dp0..\jinni-update
set JINNI_INSTALLATION_PATH="%~dp0.."
set LIBS=%JINNI_INSTALLATION_PATH%\lib

if "%JAVA_HOME%"=="" (
    set JAVA_EXECUTABLE=java    
)else (
    set JAVA_EXECUTABLE="%JAVA_HOME%\bin\java"
)

rem download jinni to jinni-update sub folder
echo Checking for update:
%JAVA_EXECUTABLE% -Djinni.suppressDone=true -Djinni.installationDir=%JINNI_INSTALLATION_PATH% -Dgm.ownCl=true -jar "%LIBS%\launch.jar" update-jinni %*

IF %ERRORLEVEL% neq 0 (
    exit /B %ERRORLEVEL%; 
)

rem replace existing installation by downloaded one if available
if exist "%JINNI_UPDATE_PATH%" (
    rem Transferring prepared Jinni to installation:
    %JAVA_EXECUTABLE% -cp "%LIBS%\jinni-update-support.jar" JinniTransfer "%JINNI_UPDATE_PATH%" "%JINNI_INSTALLATION_PATH%"
    
    IF %ERRORLEVEL% neq 0 (
        exit /B %ERRORLEVEL%;
    )
    
    echo Update complete
)

endlocal

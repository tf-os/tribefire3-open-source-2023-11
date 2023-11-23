@echo off
setlocal

if "%JAVA_HOME%"=="" (
    set JAVA_EXECUTABLE=java    
)else (
    set JAVA_EXECUTABLE="%JAVA_HOME%\bin\java"
)

%JAVA_EXECUTABLE% -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=y -Djinni.installationDir="%~dp0\..\\" -Dgm.ownCl=true -jar "%~dp0\..\lib\launch.jar" %*

endlocal

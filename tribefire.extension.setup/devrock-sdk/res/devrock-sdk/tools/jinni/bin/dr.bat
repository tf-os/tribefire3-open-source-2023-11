@echo off
setlocal
set TOOLS_DIR=%~dp0\..\..
set DEVROCK_SDK_HOME=%TOOLS_DIR%\..
set REPOSITORY_CONFIGURATION_DEVROCK_ANT_TASKS=%DEVROCK_SDK_HOME%\conf\repository-configuration-devrock.yaml

REM Setting these here, cause it's tricky to do so insde an if/else 
set GRADLE_HOME=%TOOLS_DIR%\gradle

set ANT_HOME=%TOOLS_DIR%\ant
set ANT_LIB_DIR=%TOOLS_DIR%\ant-libs

REM differentiate between gradle and ant build scripts to call according build system

if exist build.gradle (
    CALL %GRADLE_HOME%\bin\gradle.bat %GRADLE_DEBUG_OPTS% %*

) else (
    if exist build.xml (
        CALL %ANT_HOME%\bin\ant.bat -lib %ANT_LIB_DIR% %*

    ) else (
        echo current working dir is neither containing a gradle nor an ant build script
        exit /b 1
    )
)

endlocal